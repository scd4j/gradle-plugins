SCD4J stands for Simple Continuous Delivery for Java and Groovy Developer. 
-----------

In short, SCD4J is an automation platform for configuring and installing your Web IT infrastructure. With just one command you can install clusters and deploy applications. Note that SCD4J is not another option for Puppet or Chef. It is, actually, a SIMPLER option than those tools.

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
* [Advanced Configurations](https://github.com/scd4j/gradle-plugins/wiki/08.-Advanced-Configurations)
    * Shows some advanced configuration
* [Best Practices](https://github.com/scd4j/gradle-plugins/wiki/09.-Best-Practices)
    * Gives you some tips of how you should code your project 
* [FAQ](https://github.com/scd4j/gradle-plugins/wiki/10.-FAQ)
    * Frequent questions and answers
* [SCD4J Examples](https://github.com/scd4j/gradle-plugins/wiki/11.-SCD4J-Examples)
    * Describes, in a high level, each example in [scd4j-examples](https://github.com/scd4j/gradle-plugins/tree/master/scd4j-examples)


Project Structure
-------------

In short, you first must create a file `build.gradle`, like the below example:

```
plugins {
    id "com.datamaio.scd4j" version "0.6.1"
}

scd4j {
    install {
        modules "my_module_dir"         // should be any dir into modules dir
        config  "my_config_file.conf"	// should be any properties file into config dir.
    }
}
```

Second, you must create a directory `config` and another `modules`. 

```
build.gradle
config/
modules/
```

Then, inside of `modules` dir, you can create as many directories you would like to. Each one, will become a module. In our `build.gradle` example we have defined a module called `my_module_dir`, so you must create a directory `modules/my_module_dir`.

```
build.gradle
config/
modules/
      my_module_dir/
                 ...
```

In the `config` dir, you must create a file (i.e. a Java properties file) and put into it the variables that will be used by your modules. In our example, we have defined a file called `my_config_file.conf`. So, we need to have this file inside of `config` dir in order to run our project.

```
build.gradle
config/
      my_config_file.conf
modules/
      my_module_dir/
                 ...
```

Finally, we can run our project typing `gradlew` at the command line in the project directory.

**Important note:** You need to have Java 8 and Gradle 2.1 or higher installed to be able to run SCD4J projects. Please, see [Requirements](https://github.com/scd4j/gradle-plugins/wiki/01.-Requirements) for more details.

To understand more about how to implement a module take a look at the [wiki](https://github.com/scd4j/gradle-plugins/wiki/02.-Basics)

-----------

Thanks for the interest of making the softare delivery somewhat more professional. Hope you enjoy our tool.

Sincerely, SCD4J Team

