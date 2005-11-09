# JPage - Servlet Integration for Jnhp
## What is JPage?
It is a simple Servlet intended to help in the building of Web Applications. Internally uses
Jnhp tamplate library to combine Java classes and HTML content to produce the final pages.

Even that it is obsolete now, given that follows the Web 1.0 (CGI) model, its been published
here in github with the hope that still could be taken and enhanded by the community.

## Introduction
For servlet integration we need to implement the doGet/doPost in our Servlet class and then,
use the URL params to define which template to load, also note that we have to decide about
the package for the Java class associated with that template, both of these decisions will be
solved by convention, I will give details later. The form of the URL is:

```
http://server.domain.name/webpreffix/JPage?page=home.cars
```

The value **"webpreffix"** is the prefix for the application, the **JPage** is the name for the servlet
and the URI param page will define the package for the Java class and the path for the template, in our example:
**home/cars.html **

## Request and Response Integratio
The previous URL will look for the template, invoke the Java class associated (deduced from the page
URI parameter), as we have seen before, this text resolving procedure is recursive, so if the
template cars.html has more application application TAGs inside, the process will expand to those
template and classes using the same strategy.

#The HTTP Request
Every Jnhp object that is instantiated during the expansion process receives the request and response
of the Http underlying layer, the following figure shows part of the  implementation for the method
**runNodeApplication** responsible for the expansion of the template and its Java class associated.

<img height="300px" align="center" src="images/figure-1.png?raw=true">

This means that we can access the Http Request from our Java class, for example in the header.java:

<img height="300px" align="center" src="images/figure-2.png?raw=true">

Inside the implementation of the method **runApp** , we can take the parameter
**"tit"** from the request, and use its value to fill the "title" VAR tag  in the template **header.html**

## JAR Compilation
To include JPage servlet in your project, add the following XML block shown in the web.xml file.
The listing means that requests to the URI /JPage will be dispatched to our servlet:

```
    <servlet>
        <servlet-name>jnhp</servlet-name>
        <servlet-class>compulinux.jpage.JPage</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>jnhp</servlet-name>
        <url-pattern>/JPage</url-pattern>
    </servlet-mapping>
```
## Web Security (TODO)
The security has two aspects, the authentication and the authorization. The first one allows to
recognize the identity of the request using many possible mechanism like cookies, the second aspect
deals with what is accessible by the request. The JPage Servlet implements the authorization process
through the use of resources (simple strings) that represents every template.

In this way, It will be possible to access Jnhp templates that are listed in our resources list, which
could be loaded from a Database.  







