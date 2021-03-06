<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container">
    <div class="jumbotron">
        <h1>Registration successful!</h1>
        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <p><a class="btn btn-lg btn-success" href="/" role="button">Return to main page</a></p>
        </sec:authorize>
        <sec:authorize access="isAnonymous()">
            <p class="lead">To proceed, you have to validate your email. Check your mailbox for futher instructions</p>

            <p><a class="btn btn-lg btn-success" href="<c:url value="/signin"/>" role="button">Sign in today</a></p>
        </sec:authorize>
    </div>
</div>
