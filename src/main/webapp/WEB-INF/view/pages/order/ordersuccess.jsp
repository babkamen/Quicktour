<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container">
    <div class="jumbotron">
        <h1>You have successfully ordered the tour!</h1>
        <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_USER')">
            <h2>Your order request has been sent for processing to our agents.
                <p>You can always check its status on your order page</p></h2>
            <p><a class="btn btn-lg btn-success" href="<c:url value="/orders"/>" role="button">View my orders</a>
                <a class="btn btn-lg btn-success" href="<c:url value="/"/>" role="button">Return to main page</a></p>
        </sec:authorize>
        <sec:authorize access="isAnonymous()">
            <p class="lead">Your order request has been sent for processing to our agents.

            <p>

            <p><a class="btn btn-lg btn-success" href="<c:url value="/"/>" role="button">Main page</a></p>
        </sec:authorize>
    </div>
</div>

