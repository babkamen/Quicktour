<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<script src="<c:url value="/resources/js/passfield.min.js"/>"></script>
<script src="<c:url value="/resources/js/signup.js"/> "></script>
<script src="<c:url value="/resources/js/jquery.inputmask-multi.js"/> "></script>
<script src="<c:url value="/resources/js/jquery.bind-first-0.2.2.min.js"/> "></script>
<script src="<c:url value="/resources/js/jquery.inputmask.js"/> "></script>
<script src="<c:url value="/resources/js/jasny-bootstrap.min.js"/> "></script>
<script src="<c:url value="/resources/js/jquery.validate.js"/> "></script>
<script src="<c:url value="/resources/js/jquery.validator.additional-methods.js"/> "></script>
<link rel="stylesheet" href="<c:url value="/resources/css/register.css"/>"/>
<link rel="stylesheet" href="<c:url value="/resources/css/passfield.min.css"/>"/>
<link rel="stylesheet" href="<c:url value="/resources/css/jasny-bootstrap.min.css"/>"/>

<div class="row">

    <div class="col-xs-12 col-sm-8 col-md-6 col-sm-offset-2 col-md-offset-3">
        <form:form commandName="user" name="user" id="registrationForm" modelAttribute="user" method="POST"
                   cssClass="form-horizontal" role="form"
                   enctype="multipart/form-data">
        <h2>Please Sign Up
            <small>It's free and always will be.</small>
        </h2>
        <hr class="colorgraph">
        <div class="row">
            <div class="col-xs-6 col-sm-6 col-md-6">
                <div class="form-group">
                    <form:input id="name" path="name" size="30" maxlength="30" cssClass="form-control input-lg"
                                placeholder="First name" required="true" tabindex="1"/>
                    <form:errors path="name" cssClass="alert-danger"/>
                </div>
            </div>
            <div class="col-xs-6 col-sm-6 col-md-6">
                <div class="form-group">
                    <form:input name="surname" id="surname" path="surname" size="30" maxlength="30"
                                cssClass="form-control input-lg" tabindex="2"
                                placeholder="Last Name" required="true"/>
                    <form:errors path="surname" cssClass="alert-danger"/>

                </div>
            </div>
        </div>
        <div class="form-group">
            <form:input name="username" id="username" path="username" size="30" maxlength="30"
                        cssClass="form-control input-lg"
                        placeholder="Username" required="true" tabindex="3"/>
            <form:errors path="username" cssClass="alert-danger"/>
        </div>
        <div class="form-group">
                <%--<tiles:importAttribute name="short" scope="page" ignore="true"/>--%>
                <%--<c:choose>--%>
                <%--<c:when test="${ pageScope.short==null}">--%>
            <form:input name="email" id="email" path="email" size="30" maxlength="30"
                                cssClass="form-control input-lg" tabindex="4"
                                placeholder="Email Address" required="true" type="email"/>
                    <form:errors path="email" cssClass="alert-danger"/>
                <%--</c:when>--%>
                <%--<c:otherwise>--%>
                <%--<label cssClass="form-signin-heading">Email:</label>--%>
                <%--${user.email}--%>
                <%--</c:otherwise>--%>
                <%--</c:choose>--%>
        </div>
        <div class="row">
            <div class="col-xs-6 col-sm-6 col-md-6">
                <div class="form-group">
                    <form:input name="password" id="password" type="password" path="password"
                                placeholder="Password" required="true" cssClass="form-control input-lg" tabindex="5"/>
                    <form:errors path="password" cssClass="alert-danger"/>
                </div>
            </div>
            <div class="col-xs-6 col-sm-6 col-md-6">
                <div class="form-group">
                    <input type="password" name="password_confirmation" required="true" id="password_confirmation"
                           class="form-control input-lg" placeholder="Confirm Password" tabindex="6">
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-6 col-sm-6 col-md-6">
                <div class="form-group">
                    <form:input name="age" id="age" path="age" size="30" maxlength="30"
                                cssClass="form-control input-lg" tabindex="7"
                                placeholder="Age"/>
                    <form:errors path="age" cssClass="alert-danger"/>
                </div>
            </div>
            <div class="col-xs-6 col-sm-6 col-md-6">
                <div class="form-group">
                    <form:input name="companyCode" id="companyCode" path="companyCode" size="30" maxlength="30"
                                cssClass="form-control input-lg" tabindex="8"
                                placeholder="Company code"/>
                    <form:errors path="companyCode" cssClass="alert-danger"/>
                </div>
            </div>
        </div>
        <div class="form-group">
            <form:input id="phone" path="phone" size="30" maxlength="30"
                        cssClass="form-control input-lg"
                        placeholder="Phone" required="true" tabindex="9"/>
            <form:errors path="phone" cssClass="alert-danger"/>
        </div>
        <div class="form-group">
            <select name="gender" class="form-control input-lg" tabindex="10">
                <option value="" disabled selected>
                    Gender
                </option>
                <option value="Male">
                    Male
                </option>
                <option value="Female">
                    Female
                </option>
            </select>
        </div>
        <div class="form-group">
            <div class="fileinput fileinput-new" data-provides="fileinput">
                <div class="fileinput-preview fileinput-exists thumbnail"
                     style="max-width: 200px; max-height: 150px;"></div>
                <div>
                    <span class="btn btn-default btn-file"><span class="fileinput-new">Select image</span><span
                            class="fileinput-exists">Change</span><input type="file" accept="image/*" name="avatar"
                                                                         tabindex="11"></span>
                    <a href="#" class="btn btn-default fileinput-exists" data-dismiss="fileinput">Remove</a>
                </div>
            </div>
            <form:errors path="photo" cssClass="alert-danger"/>
        </div>
        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <div class="form-group">
                <form:select path="role" class="form-control input-lg">
                    <c:forEach items="${roles}" var="role">
                        <form:option value="${role.roleId}">${role.name}</form:option>
                    </c:forEach>

                </form:select>
            </div>
        </sec:authorize>
        <hr class="colorgraph">
        <div class="row">
            <input type="submit" value="Register"
                   class="btn btn-primary btn-block btn-lg" tabindex="12"></div>
    </div>
    </form:form>
</div>