# svja
The RDPS Super Vulnerable Java Application is just that -- a super vulnerable java application created for the benefit of all to see a variety of common, often "complex", vulnerabilities. This software was created as part of the Roniel and DaRon Podcast Show (https://www.youtube.com/channel/UCbj1JFcSJeTuf3qf9GPRemA) so that we could discuss and show a number of vulnerabilities that we came across frequently in a more realistic way than what you will see with the many other tools online. To see example exploitation, please watch our podcast.

## IMPORTANT ##
Please remember that this application is intended to be highly vulnerable to a number of things. Be careful where you run it, limit who can access it (the Tomcat server should only listen on localhost and only ever on your personal machine). We are not responsible if you can't take proper care of your things, leaving your copy of it hanging out in the wind for anyone and everyone to steal your stuff or horribly compromise your machine. We recommend that you run this in a virtual machine (VM) that is secured and that has nothing sensitive on it and that has no connectivity to the internet. Failure to properly secure the system running this and the instance of it will likely result in a bad day for you. Like casual carnal relations without proper protections, you are only putting yourself in a position that you will come to regret someday. Consider yourself warned, but encouraged to play ;-)

## Requirements ##
Apache Maven 3.6 or above
npm 7.11 or above
Java 11 or above
Tomcat 9.0

## Installation ##
The RDPS SVJA is composed of a Java struts 2 application running react. It is built by simply running:
mvn clean package

This creates a WAR in target -- svja.war. Simply place the WAR in your tomcat webapps folder. In a default Tomcat build go to http://127.0.0.1:8080/svja to access the application.

## Current vulnerabilities ##
/api/projects
	Cross-site scripting (XSS) via encoding
	Insecure deserialisation (OGNL, Struts, JSON -- no bytecode)
		Path traversal (read-only)
		Arbitrary object creation
		Arbitrary method call
		Session hijacking
		Session bypass
		Authentication bypass
		Insecure direct object reference
	Insecure serialisation
		Personally identifiable information (PII) bleed
		Sensitive information bleed
	Denial of service

## Remember ##
This is a work-in-progress and we have many more vulnerabilities and things planned (to include a super vulnerable frontend and other super vulnerable applications) -- check back with us every couple of months and watch the Roniel and DaRon Podcast Show for updates, sample exploitation, and other ideas. We also encourage you to try to figure out how to mitigate or fix the vulnerabilities... breaking things is easy... it's mitigating, fixing, and securing that is hard.  Good luck and happy hunting!
