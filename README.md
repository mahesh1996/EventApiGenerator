# EventApiGenerator
Java Program to generate apis of event registration system in php.

###Usage : 

clone the repo

```
git clone https://github.com/mahesh1996/EventApiGenerator.git
cd EventApiGenerator
javac *.java
java GenerateApi --name=quiz --userTableName=user --no=3
```

It will generate quiz.php file in same folder
> **Note:** Consider below points about arguments to GeneratApi
> - **--name** is the name of the table in database and the name of the generated php file will be same.
> - **--userTableName** is the name of the table in database used to store user's login credential such as email and password.
> - **--no** is the no of participant in the current event.
> - You must set ```$_SESSION['isLoggedIn']``` to the email of the user being logged when user logs in to your application.
> - All of the user trying to register in event must be first registered on the website and their log in information(email and password) must be stored in table provided by **--userTableName**

Ok, Now Consider below Example to understand it clearly.
Suppose you are designing event registration system for your college and there are 2 events.  
> 1. **Quiz** in which maximum 3 person can take part.  
> 2. **Coding** in which only one person can take part means it is single person event.   
Now run the following command to generate the apis.
```
java GenerateApi --name=quiz --userTableName=user --no=3
java GenerateApi --name=coding --userTableName=user --no=1
```
It will generate two php files, quiz.php and coding.php  
  
  
###Usage of api.  
Consider that you've hosted this api on domain www.xyz.com
Then your urls will become like this
> www.xyz.com/quiz.php  
> www.xyz.com/coding.php.  

Now, **--userTableName** which you've provided is the name of table in which user's email and password is stored with column name **email** 
and **--name** is the name of the table in which registerd user's information will be stored.  
So, in our case there must be table with name **user** which will store the information of user with atleast one primary key **email** 
and we will have another two table **quiz** and **coding**.  
Now, every event table have **--no** of columns starting with **email1** , **email2** , ... an so on upto **--no* and also one **id** primary key with auto_increment.  
In our case **coding** is single person event, so there must be coding table with two columns.  

> 1. **id** int primary key Auto_increment.  
> 2. **email1** varchar2(255).   

And for quiz, we will have **quiz** table with four columns.  

> 1. **id** int primary key Auto_increment.  
> 2. **email1** varchar2(255) for email of partner 1.  
> 3. **email2** varchar2(255) for email of partner 2.  
> 4. **email3** varchar2(255) for email of partner 3.  

###How to call Api?.  
After creating tables as shown above, you will have to create another php file for storing of database credentials as follows.  
Create another php file, say connection.php and in that file add following variables.   
> 1. **```$host```** set it to the database host(e.g localhost)
> 2. **```$db```** name of the database
> 3. **```$dbuser```** username of the database
> 4. **```$dbpassword```** password of the database

Include this file in every api file at the top.  
One more thing  to remember is that whenever user logs in to your system, you will have to set **```$_SESSION['isLoggedIn']```** to the email of the user being logged.  
Now, It provides following APIs.  
> 1. For single person event
>   - set the action variable to getstatus to get status of the user(e.g www.xyz.com/coding.php?action=getstatus)  
      It will return following response  
      ```
      {'status' : 'registered'} (in case user is registered)  
      ```  
      ```
      {'status' : 'unregistered'} (in case user is not registered)
      ```  
>   - set the action variable to register to register the user(e.g www.xyz.com/coding.php?action=register)  
      It will return following response   
      ```
      {'status' : 'registered'} (in case user is successfully registered or already registered)
      ```  
      ```
      {'status' : 'error'} (in case any error has occurred)
      ```
>   - set the action variable to unregister to unregister the user(e.g www.xyz.com/coding.php?action=unregister)  
      It will return following response   
      ```
      {'status' : 'unregistered'} (in case user is successfully unregistered or not registered)
      ```  
      ```
      {'status' : 'error'} (in case any error has occurred)
      ```
> 2. For Multi Person event( Event in which more than one person are allowed.)
>	- set the action variable to getstatus to get status of user(e.g www.xyz.com/quiz.php?action=getstatus)
	  It will return following response  
	  ```
	  {'status' : 'registered',
	   'partner_email1' : 'email_of_partner1'
	   'partner_email2' : 'email_of_partner2'
	   and so on up to n-1 where n is no of participant or null in case there are less no of partners
	  }
	  ```  
	  So in our case, in quiz 3 no of total particiants are there. So if all three are registered then it will give following response. Email of third logged in partner is stored in $_SESSION['isLoggedIn'].  
	  ```  
	  {'status' : 'registered',
	   'partner_email1' : 'email_of_partner1'
	   'partner_email2' : 'email_of_partner2'
	  }
	  ```  
	  If only two person are registered in this event then it will give following response.  
	  ```
	  {'status' : 'registered',
	   'partner_email1' : 'email_of_partner1'
	   'partner_email2' : null
	  }
	  ```  
	  If user is not registered, it will give following response.  
	  ```
	  {'status' : 'unregistered'} (in case user is not registered)
	  ```
>	- set the action variable to register to register in the event algon with the partner's email as shown in this url (e.g www.xyz.com/qiz.php?action=register&partner_email1=email_of_first_partner&partner_email2=email_of_second_partner and so on...)  
	   If you want to register with less no of user, then specify **no_email** as email of other user(e.g www.xyz.com/quiz.php?action=register&partner_email1=email_of_first_partner&partner_email2=no_email). This will register you only with one partner in this event.  
	   It will return following response.  
	   ```
	   {'partner1_status' : 'yourself' and so on..} (in case if you try to register yourself as your participant)  
	   ```  
	   ```
	   {'partner1_status' : 'not_registered',
	    'partner2_status' : 'not_registered'
	    and so on
	   } (in case if partner1 and partner2 is not registerd on this website)  
	   ```  
	   ```  
	   {'partner1_status' : 'already_registered_with_other',
	    'partner2_status' : 'already_registered_with_other'
	    and so on
	   } (in case if partner1 and partner2 has already registered with other user in the same event)  
	   ```  
	   ```
	   {'status' : 'registered'} (in case user has registered successfully).
	   ```
>	- set the action variable to unregister to unregister user and its partners from this event(e.g www.xyz.com?action=unregister)  
	  It will return follwoing response.  
	  ```
	  {'status' : 'unregistered'} (in case user and its participants are unregsitered)
	  ```  
	  ```
	  {'status' : 'error'} (in case any error occurred)
	  ```  