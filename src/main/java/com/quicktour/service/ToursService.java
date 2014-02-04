package com.quicktour.service;

import com.quicktour.Roles;
import com.quicktour.dto.DiscountPoliciesResult;
import com.quicktour.entity.Company;
import com.quicktour.entity.Tour;
import com.quicktour.entity.TourInfo;
import com.quicktour.entity.User;
import com.quicktour.repository.CommentRepository;
import com.quicktour.repository.CompanyRepository;
import com.quicktour.repository.ToursRepository;
import com.quicktour.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

//import org.springframework.data.domain.PageImpl;

@Service
@Transactional
public class ToursService {
    private final int NUMBER_OF_RECORDS_PER_PAGE = 4;
    private static final int NUMBER_OF_FAMOUS_TOURS = 3;
    final Logger logger = LoggerFactory.getLogger(ToursService.class);
    @Autowired
    UsersService usersService;
    @Autowired
    CompanyService companyService;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    DiscountPolicyService discountPolicyService;
    @Autowired
    UserRepository userRepository;
    EntityManager entityManager;
    @Autowired
    private ToursRepository toursRepository;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Tour findOne(Integer id) {
        return toursRepository.findOne(id);
    }

    public List<Tour> saveTours(List<Tour> tours) {
        return toursRepository.save(tours);
    }

    public List<Tour> getTours(Integer[] tourIds) {
        return toursRepository.findByTourIdIn(Arrays.asList(tourIds));
    }

    /**
     * Save tour to database
     *
     * @param tour the tour entity for save
     * @return saved tour
     */
    public Tour saveTour(Tour tour) {
        return toursRepository.saveAndFlush(tour);
    }

    /**
     * @return all tours from database
     */
    public List<Tour> findAllTours() {
        return toursRepository.findAll();
    }

    /**
     * search tours created by agency of the current logged in agent
     *
     * @return list of tour, which current agent can manage
     * if current user is admin return all tours
     */
    public List<Tour> findAgencyTour() {
        User user = usersService.getCurrentUser();
        if (user.getRole() == Roles.agent) {
            Company company = companyRepository.findByCompanyCode(user.getCompanyCode());
            return (List<Tour>) company.getTours();
        }
        return findAllTours();
    }

    /**
     * search all tours and paginate it
     *
     * @param pageNumber number of page for pagination
     * @return selected tour page
     */
    public Page<Tour> findAllTours(int pageNumber) {
        Page<Tour> tours = toursRepository.findByActiveTrue(new PageRequest(pageNumber, NUMBER_OF_RECORDS_PER_PAGE));
        logger.debug("Current user find all tours {}.Discount policies {}", usersService.getCurrentUser(), tours.getContent().get(0).getDiscountPolicies());
        loadRate(tours);
        return tours;
    }

    private void loadRate(Iterable<Tour> tours) {
        for (Tour tour : tours) {
            Long rateCount = tour.getRateCount();
            if (rateCount != null && rateCount > 0) {
                tour.getRate();
            }
        }
    }

    public Page<Tour> findAllToursAndCut(int pageNumber) {
        Page<Tour> tourPage = findAllTours(pageNumber);
        List<Tour> toursList = tourPage.getContent();
        toursList = (List<Tour>) cutToursDescription(toursList);
        DiscountPoliciesResult discountPoliciesResult = null;
        User user = usersService.getCurrentUser();
        for (Tour tour : toursList) {
            if (user == null) {
                tour.setDiscount(BigDecimal.ZERO);
            } else {
                discountPoliciesResult = discountPolicyService.calculateDiscount(tour.getDiscountPolicies());
                tour.setDiscount(discountPoliciesResult.getDiscount());
                tour.setDiscountPolicies(discountPoliciesResult.getDiscountPolicies());
            }
        }
        return tourPage;
    }

    public Tour findTourById(int id) {
        return toursRepository.findByTourId(id);
    }

    /**
     * search tours by country
     *
     * @param country
     * @param pageNumber number of page for pagination
     * @return tour page with search results
     */
    public Page<Tour> findToursByCountry(String country, int pageNumber) {
        Page<Tour> tours = toursRepository.findToursByCountry(country,
                new PageRequest(pageNumber, NUMBER_OF_RECORDS_PER_PAGE));
        loadRate(tours);
        return tours;
    }

    public Page<Tour> findToursByCountryAndCut(String country, int pageNumber) {
        Page<Tour> tours = findToursByCountry(country, pageNumber);
        cutToursDescription(tours.getContent());
        return tours;
    }

    public Page<Tour> findToursByPlaces(String placeName, int pageNumber) {
        Page<Tour> tours = toursRepository.findToursByPlaceName(placeName,
                new PageRequest(pageNumber, NUMBER_OF_RECORDS_PER_PAGE));
        loadRate(tours);
        return tours;
    }

    public Page<Tour> findToursByPlacesAndCut(String placeName, int pageNumber) {
        Page<Tour> tours = findToursByPlaces(placeName, pageNumber);
        cutToursDescription(tours.getContent());
        return tours;
    }

    public Page<Tour> findToursByPrice(int minPrice, int maxPrice, int pageNumber) {
        Page<Tour> tours = toursRepository.findToursByPrice(new BigDecimal(minPrice), new BigDecimal(maxPrice),
                new PageRequest(pageNumber, NUMBER_OF_RECORDS_PER_PAGE));
        loadRate(tours);
        return tours;
    }

    public Page<Tour> findToursByPriceAndCut(int minPrice, int maxPrice, int pageNumber) {
        Page<Tour> tours = findToursByPrice(minPrice, maxPrice, pageNumber);
        cutToursDescription(tours.getContent());
        return tours;
    }

    /**
     * search tours by entered by users parameters
     *
     * @param country
     * @param place
     * @param minDate
     * @param maxDate
     * @param minPrice
     * @param maxPrice
     * @param pageNumber number of page for pagination
     * @return
     */
    public Page<Tour> extendFilter(String country, String place,
                                   java.sql.Date minDate, java.sql.Date maxDate,
                                   Integer minPrice, Integer maxPrice,
                                   int pageNumber) {
        boolean firstQueryParam = true;
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select distinct t from Tour as t inner join t.toursPlaces as p " +
                "inner join t.tourInfo as ti where");

        firstQueryParam = addElementToQuery(country, firstQueryParam, strBuilder, "p.country='");
        firstQueryParam = addElementToQuery(place, firstQueryParam, strBuilder, "p.name='");
        firstQueryParam = addElementToQuery(minDate, firstQueryParam, strBuilder, "ti.startDate>'");
        firstQueryParam = addElementToQuery(maxDate, firstQueryParam, strBuilder, "ti.startDate<'");
        firstQueryParam = addElementToQuery(minPrice, firstQueryParam, strBuilder, "t.price>'");
        addElementToQuery(maxPrice, firstQueryParam, strBuilder, "t.price<'");

        if (!firstQueryParam) {
            Query query = entityManager.createQuery(strBuilder.toString());
            List<Tour> results = query.getResultList();
            int totalNumberOfResults = results.size();
            if (totalNumberOfResults == 0) {
                return null;
            }
            Page<Tour> tourPage = new PageImpl<Tour>(results,
                    new PageRequest(pageNumber, NUMBER_OF_RECORDS_PER_PAGE),
                    totalNumberOfResults);
            loadRate(tourPage);
            return cutToursOnPage(tourPage);
        }
        return null;
    }

    private boolean addElementToQuery(Object element, boolean firstQueryParam, StringBuilder strBuilder, String strElement) {
        if (element != null && element.toString().length() > 0) {
            strBuilder.append((firstQueryParam) ? " " : " and ").
                    append(strElement).append(element.toString()).append("'");
            return false;
        } else {
            return firstQueryParam;
        }

    }

    public String cutDescription(String description) {
        final int DEFAULT_LENGTH = 125;
        if (description.length() > DEFAULT_LENGTH) {
            description = description.substring(0, DEFAULT_LENGTH) + "...";
        }
        return description;
    }

    private Collection<Tour> cutToursDescription(Collection<Tour> tours) {
        for (Tour tour : tours) {
            String desc = tour.getDescription();
            desc = cutDescription(desc);
            tour.setDescription(desc);
            //tour.setPrice((BigDecimal) findMinPrice(tour));
        }
        return tours;
    }

    /**
     * search min price for tour
     * min price calculated as minimal value of all tour prices
     *
     * @param tour tour for which price will be calculate
     * @return minimum price value
     */
    public BigDecimal findMinPrice(Tour tour) {
        BigDecimal price = tour.getPrice();
        double maxDiscount = findMaxDiscount(tour);
        price = price.subtract(price.multiply(BigDecimal.valueOf(maxDiscount / 100)));
        return price;
    }

    public double findMaxTourDiscount(Tour tour) {
        List<TourInfo> tourByDate = (List<TourInfo>) tour.getTourInfo();
        double maxDiscount = tourByDate.get(0).getDiscount();
        for (TourInfo tourInfo : tourByDate) {
            Double discount = tourInfo.getDiscount().doubleValue();
            if (discount > maxDiscount) {
                maxDiscount = discount;
            }
        }
        return maxDiscount;
    }

    public double findMaxDiscount(Tour tour) {
        BigDecimal maxTourDiscount = new BigDecimal(findMaxTourDiscount(tour));
        DiscountPoliciesResult discountPoliciesResult = discountPolicyService.calculateDiscount(tour.getDiscountPolicies());
        BigDecimal totalDiscount = maxTourDiscount.add(discountPoliciesResult.getDiscount());
        BigDecimal companyDiscount = companyService.getCompanyDiscount(usersService.getCurrentUser());
        if (companyDiscount.doubleValue() > 0) {
            totalDiscount = totalDiscount.add(companyDiscount);
        }
        return totalDiscount.doubleValue();
    }

    public Page<Tour> cutToursOnPage(Page<Tour> tourPage) {
        cutToursDescription(tourPage.getContent());
        return tourPage;
    }

    public List<Tour> findFamousTours() {
        return toursRepository.findFamousTours(new PageRequest(0, NUMBER_OF_FAMOUS_TOURS)).getContent();
    }

    /**
     * Finds agency's tours
     *
     * @param empty true if you need tours without discount policies and false if you need tours with discount policies
     */
    public List<Tour> findAgencyToursWithDiscountPoliciesAreEmpty(boolean empty) {
        User currentUser = usersService.getCurrentUser();
        Company company = companyService.getCompanyByUserId(currentUser.getUserId());
        List<Tour> tours;
        if (empty) {
            tours = toursRepository.findByCompanyAndDiscountPoliciesIsEmpty(company);
        } else {
            tours = toursRepository.findByCompanyAndDiscountPoliciesIsNotEmpty(company);
        }
        prepareTour(!empty, tours);
        return tours;
    }

    public List<Tour> prepareTour(boolean initializeDiscountPolicies, List<Tour> tours) {
        List<Tour> toursResult = new ArrayList<>(tours);
        for (Tour tour : toursResult) {
            tour.setPriceIncludes(null);
            tour.setToursPlaces(null);
            if (initializeDiscountPolicies) {
                tour.getDiscountPolicies().size();
            }
        }
        return toursResult;
    }

    public Tour findTourByIdWithPlaces(int id) {
        Tour tour = findTourById(id);
        logger.debug("Tour places {}", tour.getToursPlaces().size());

        return tour;
    }

    public Tour findTourByIdWithCompany(int id) {
        Tour tour = toursRepository.findOne(id);
        tour.getCompany().getAddress();
        logger.debug("Tour places {}", tour.getPriceIncludes().size());
        return tour;
    }

    public Tour findTourByIdWithPlacesAndPriceIncludes(int id) {
        Tour tour = toursRepository.findOne(id);
        logger.debug("Tour places {}", tour.getPriceIncludes().size());
        tour.getToursPlaces().size();
        return tour;
    }

}
