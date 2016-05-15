![SCD4J](https://avatars0.githubusercontent.com/u/9500996?v=3&s=80) | <p>SCD4J Stands for: <br> Simple Continuous Delivery for Java and Groovy Developers</p>
------------- |:-------------

In short, SCD4J is an automation platform for configuring and installing your Web IT infrastructure. With just one command you can install clusters and deploy applications. Note that SCD4J is not another option for Puppet or Chef. It is, actually, a SIMPLER option than those tools 


Documentation
-----------

To understand more about SCD4J or get more details about how to create and implement a new project, take a look at documentation:

* [Home](https://github.com/scd4j/gradle-plugins/wiki)
    * Gives you the introduction and advantages of using this tool over the existing ones 
* [Requirements](https://github.com/scd4j/gradle-plugins/wiki/01.-Requirements)
    * Minimum requirements to run this tool 
* [Basics](https://github.com/scd4j/gradle-plugins/wiki/02.-Basics)
    * Explains how to create a new SCD4J project 
* [Managing dependencies](https://github.com/scd4j/gradle-plugins/wiki/03.-Managing-dependencies)
    * Shows the options to manage your dependencies 
* [How to run my project](https://github.com/scd4j/gradle-plugins/wiki/04.-How-to-run-my-project)
    * Demostrate how you should run your SCD4J project
* [Encryption Tools](https://github.com/scd4j/gradle-plugins/wiki/05.-Encryption-Tools)
    * Shows the tools SCD4J provides to encrypt your information
* [Packaging and Distribution](https://github.com/scd4j/gradle-plugins/wiki/06.-Packaging-and-Distribution)
    * Explains how you should pack and distribute your project 
* [Logging and Backup](https://github.com/scd4j/gradle-plugins/wiki/07.-Logging-and-Backup)
    * Talks about logs, backups and a suggested clean up policy
* [Advanced Features](https://github.com/scd4j/gradle-plugins/wiki/08.-Advanced-Features)
    * Shows some advanced features and configurations
* [Best Practices](https://github.com/scd4j/gradle-plugins/wiki/09.-Best-Practices)
    * Gives you some tips of how you should code your project 
* [FAQ](https://github.com/scd4j/gradle-plugins/wiki/10.-FAQ)
    * Frequent questions and answers
* [SCD4J Examples](https://github.com/scd4j/gradle-plugins/wiki/11.-SCD4J-Examples)
    * Describes, in a high level, each example in [scd4j-examples](https://github.com/scd4j/gradle-plugins/tree/master/scd4j-examples)


Project Structure
-------------

To start a new project, first we must create a file called `build.gradle`, as show in the below example:

```
plugins {
    id "com.datamaio.scd4j" version "0.7.10"
}

scd4j {
    install {
        modules "my_first_module"         // should be any dir into modules dir
        config  "my_first_config.conf"	 // should be any properties file into config dir.
    }
}
```

Then, we may run the task `newproject` of our `build.gradle` using the installed gradle build tool. 

Note: We need to have [Gradle 2.1 (or higher)](https://services.gradle.org/distributions/gradle-2.2.1-all.zip) installed just for the first run. After that, SCD4J will automatically install a Gradle wrapper. Please, see [Requirements](https://github.com/scd4j/gradle-plugins/wiki/01.-Requirements) for more details.

Once the execution has finished we will have the following directory structure created:

```
build.gradle
config/
      my_first_config.conf
modules/
      my_first_module/
            Module.hook
gradlew
gradlew.bat
gradle/...
```

The `config` dir contains the configuration files. In our case the `my_first_config.conf` file (i.e. a Java properties file) in which we must put all the variables that will be used by our modules.

The `modules` dir contains a new module called `my_first_module`. This is the place where we will implement our automation. Note that the `newproject` task also created a file called `Module.hook`, take a look at [basics](https://github.com/scd4j/gradle-plugins/wiki/02.-Basics) to undertand how to implement this hook.

Finally, we can see a `gradle` directory and `gradlew` and `gradlew.bat` files. Those are the gradle wrapper and, from now one, you can pack this project and run it in any machine without needing to install gradle.

To understand more about how to implement a module take a look at the [wiki](https://github.com/scd4j/gradle-plugins/wiki/02.-Basics)

-----------

Thanks for the interest of making the software delivery somewhat more professional. Hope you enjoy our tool.

Sincerely, SCD4J Team
-
