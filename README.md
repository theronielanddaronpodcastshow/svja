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
Below are some of the _major_ vulnerabilities in the system, by action.

1. /api/authenticate
   - Cross-site request forgery (CSRF)
   - Denial of service (DOS)
   - Insecure deserialisation (OGNL, Struts, JSON -- no bytecode)
	 - Arbitrary object creation
	 - Arbitrary method call
	 - Session hijacking
	 - Session bypass
	 - Authentication bypass
   - Log injection
   - Sensitive data in URL
2. /api/files
   - Cross-site request forgery (CSRF)
   - Cross-site scripting (XSS) via encoding
   - Denial of service (DOS)
   - Insecure deserialisation (OGNL, Struts, JSON -- no bytecode)
	 - Path traversal (read-only)
	 - Arbitrary object creation
	 - Arbitrary method call
	 - Session hijacking
	 - Session bypass
	 - Authentication bypass
	 - Insecure direct object reference
   - Insecure serialisation
	 - Personally identifiable information (PII) bleed
	 - Sensitive information bleed
   - Log injection
   - Sensitive data in URL
3. /api/projects
   - Cross-site request forgery (CSRF)
   - Cross-site scripting (XSS) via encoding
	 Denial of service (DOS)
   - Insecure deserialisation (OGNL, Struts, JSON -- no bytecode)
	 - Path traversal (read-only)
	 - Arbitrary object creation
	 - Arbitrary method call
	 - Session hijacking
	 - Session bypass
	 - Authentication bypass
	 - Insecure direct object reference
   - Insecure serialisation
	 - Personally identifiable information (PII) bleed
	 - Sensitive information bleed
   - Log injection
   - Sensitive data in URL

## Default installed users ##
```
admin:admin (application administrator account)
bill:baker (privileged user account)
bob:barker (basic, read-only user account)
```

## Logging into the system ##
```
/api/authenticate?user.username=admin&user.password=admin
/api/authenticate?user.username=bill&user.password=baker
/api/authenticate?user.username=bob&user.password=barker
```

After authentication, hit any action you please using either JSON or Struts

## Sample Struts authentication request ##
```
GET /svja/api/authenticate?user.username=bill&user.password=baker HTTP/1.1
User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0
Host: localhost
Accept: application/json, text/plain, */*
Accept-Language: en-GB,en;q=0.5
Accept-Encoding: gzip, deflate
Content-Length: 0
DNT: 1
Connection: close
Cache-Control: max-age=0


```

## Sample JSON authentication request ##
```
GET /svja/api/authenticate HTTP/1.1
User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0
Host: localhost
Accept: application/json, text/plain, */*
Accept-Language: en-GB,en;q=0.5
Accept-Encoding: gzip, deflate
Content-Type: application/json
Content-Length: 61
DNT: 1
Connection: close
Cache-Control: max-age=0

{
  "user": {
    "username": "bill",
    "password": "baker"
  }
}
```

## Sample Struts request ##
Remember to change the svjatoken
```
GET /svja/api/projects?projectId=1 HTTP/1.1
Host: localhost
User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0
Accept: application/json, text/plain, */*
Accept-Language: en-GB,en;q=0.5
Accept-Encoding: gzip, deflate
DNT: 1
Connection: close
Cookie: svjatoken=RaMu5vhFBgL8goV6Ja1ffLnpmcixNuAL7fUP; Path=/; HttpOnly
Cache-Control: max-age=0


```

## Sample JSON request ##
Remember to change the svjatoken 
```
GET /svja/api/projects HTTP/1.1
Host: localhost
User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0
Accept: application/json, text/plain, */*
Accept-Language: en-GB,en;q=0.5
Accept-Encoding: gzip, deflate
Content-Type: application/json
Content-Length: 19
DNT: 1
Connection: close
Cookie: svjatoken=RaMu5vhFBgL8goV6Ja1ffLnpmcixNuAL7fUP; Path=/; HttpOnly
Cache-Control: max-age=0

{
  projectId:1
}
```

## Remember ##
This is a work-in-progress and we have many more vulnerabilities and things planned (to include a super vulnerable frontend and other super vulnerable applications) -- check back with us every couple of months and watch the Roniel and DaRon Podcast Show for updates, sample exploitation, and other ideas. We also encourage you to try to figure out how to mitigate or fix the vulnerabilities... breaking things is easy... it's mitigating, fixing, and securing that is hard.  Good luck and happy hunting!
