package com.quicktour.service;

import com.quicktour.dto.LoginzaResponse;
import com.quicktour.entity.Company;
import com.quicktour.entity.Role;
import com.quicktour.entity.User;
import com.quicktour.entity.ValidationLink;
import com.quicktour.repository.CompanyRepository;
import com.quicktour.repository.UserRepository;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Contains all functional logic connected with users, such as:
 * registrating new user, getting current logged in user, updating
 * user's profile, creating registration email which will be sent
 * to every new user, functionality connected with user roles, updating
 * user's profile with new generated password and setting user active
 * or inactive
 *
 * @author Andrew Zarichnyi
 * @version 1.0 12/27/2013
 */
@Service("myUserService")
@Transactional
public class UsersService {
    private static final String TOUR_AGENCY = "Tour Agency";
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UsersService.class);
    @Autowired
    StandardPasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    ValidationService validationService;
    @Autowired
    private PhotoService photoService;
    @Autowired
    EmailService emailService;
    @Autowired
    private UserDetailsManager manager;
    private MessageDigest md5;
    @Value("${loginzaID}")
    private String loginzaID;
    @Value("${loginzaSecretKey}")
    private String loginzaKey;

    @PostConstruct
    public void init() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Cannot find md5 algorithm.{}", e);
        }

    }


    /**
     * Finds and deletes users that wasn't activated within 2 days every 2 days
     */
    @Scheduled(cron = "0 0 0 0/2 * *")
    public void deleteExpiredNotActiveUsers() {
        userRepository.deleteExpiredNotActiveUsers();
    }


    public User registerUser(User user) {

        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            Role newRole = checkIfAgent(user.getCompanyCode()) ?
                    new Role(Role.ROLE_AGENT) : new Role(Role.ROLE_USER);
            user.setRole(newRole);
        }
        cleanUser(user);
        User savedUser = userRepository.saveAndFlush(user);
        logger.info("Registered new user  {} with role {}", user.getUsername(), user.getRole());
        return savedUser;

    }

    private void cleanUser(User user) {
        user.setCompanyCode(Jsoup.clean(user.getCompanyCode(), Whitelist.simpleText()));
        user.setEmail(Jsoup.clean(user.getEmail(), Whitelist.simpleText()));
        user.setUsername(Jsoup.clean(user.getUsername(), Whitelist.simpleText()));
        user.setName(Jsoup.clean(user.getName(), Whitelist.simpleText()));
        user.setPhone(Jsoup.clean(user.getPhone(), Whitelist.simpleText()));
        user.setSurname(Jsoup.clean(user.getSurname(), Whitelist.simpleText()));
        user.setGender(Jsoup.clean(user.getGender(), Whitelist.simpleText()));
    }

    /**
     * Returns current logged in user
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName());
    }

    private boolean checkIfAgent(String companyCode) {
        Company company = companyRepository.findByCompanyCode(companyCode);
        return companyCode != null && company != null && company.getType().equals(TOUR_AGENCY);
    }


    /**
     * Activates user's profile so he can log into the system
     *
     * @param user -  user to be activated
     */
    public void activate(User user) {
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
    }


    /**
     * Updates user's company code and role in database due to new company code.
     *
     * @param newCompanyCode - String which contains new company code of the user.
     */
    public void updateCompanyCode(String newCompanyCode) {
        User user = getCurrentUser();
        user.setCompanyCode(Jsoup.clean(newCompanyCode, Whitelist.simpleText()));
        int roleId = user.getRole().getRoleId();
        if (roleId != Role.ROLE_ADMIN && roleId != Role.ROLE_AGENT &&
                checkIfAgent(user.getCompanyCode())) {
            user.setRole(new Role(Role.ROLE_AGENT));
        }
        userRepository.saveAndFlush(user);
        logger.info("User " + user.getUsername() + " " + "has changed his company code to '"
                + user.getCompanyCode() + "'");
    }

    public void changePassword(String newPassword, User user) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);
        emailService.sendPasswordSuccessRecoveryEmail(user);
    }


    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findOne(Integer id) {
        return userRepository.findOne(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user, MultipartFile image) {
        if (!image.isEmpty()) {
            user.setPhoto(null);
            photoService.saveImageAndSet(user, image);
        }
        return userRepository.saveAndFlush(user);
    }

    public User save(User user) {
        return userRepository.saveAndFlush(user);
    }

    public void recoverPassword(User user) {
        ValidationLink passwordChangeLink = validationService.createPasswordChangeLink(user);
        emailService.sendPasswordRecoveryEmail(user, passwordChangeLink);

    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public void edit(User user, MultipartFile image) {
        User existingUser = findOne(user.getUserId());
        user.setPassword(existingUser.getPassword());
        save(user, image);
    }

    public void automatedLogin(User user) {
        UserDetails userDetails = manager.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    public LoginzaResponse getResponseFromLoginza(String token) throws IOException {
        String sig = token + "" + loginzaKey;
        try {
            md5.update(sig.getBytes("utf-8"), 0, sig.length());
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsuported encoding while generating md5 . {}", e);
        }
        sig = new BigInteger(1, md5.digest()).toString(16);
        String url1 = "http://loginza.ru/api/authinfo?token=" + token + "&id=" + loginzaID + "&sig=" + sig;
        logger.debug("URL :  {}", url1);
        URL url = new URL(url1);
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        logger.debug("URL for loginza response :{}", body);
        return null; //mapper.readValue(body, LoginzaResponse.class);
    }
}
