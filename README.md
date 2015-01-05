SCD4J stands for Simple Continuous Delivery for Java and Groovy Developer. 
=============

In short, SCD4J is an automation platform for configuring and installing your Web IT infrastructure. With just one command you can install clusters and deploy applications. Note that SCD4J is not another option for Puppet or Chef. It is, actually, a SIMPLER option than those tools.

To know more about SCD4J or get more details about how to create a new project take a look at:

* [Home](https://github.com/scd4j/gradle-plugins/wiki)
    * Gives you the introduction and advantages of using this tool over the existing ones 
* [Requirements](https://github.com/scd4j/gradle-plugins/wiki/01.-Requirements)
    * Minimum requirements to run this tool 
* [Basics](https://github.com/scd4j/gradle-plugins/wiki/02.-Basics)
    * Explains, in a high level, how to create a new SCD4J project 
* [Managing dependencies](https://github.com/scd4j/gradle-plugins/wiki/03.-Managing-dependencies)
    * Shows the options to manage your dependencies packages 
* [How to run my project](https://github.com/scd4j/gradle-plugins/wiki/04.-How-to-run-my-project)
    * Demostrate how you should run your SCD4J project
* [Encryption Tools](https://github.com/scd4j/gradle-plugins/wiki/05.-Encryption-Tools)
    * Shows the tools SCD4J provides to encrypt your information to avoid not allowed people to read it 
* [Packaging and Distribution](https://github.com/scd4j/gradle-plugins/wiki/06.-Packaging-and-Distribution)
    * Explains how you should pack and distribute your SCD4J project 
* [Logging and Backup](https://github.com/scd4j/gradle-plugins/wiki/07.-Logging-and-Backup)
    * Talks about logs, backups and suggested clean up policy
* [Advanced Configurations](https://github.com/scd4j/gradle-plugins/wiki/08.-Advanced-Configurations)
    * Shows some advanced configuration
* [Best Practices](https://github.com/scd4j/gradle-plugins/wiki/09.-Best-Practices)
    * Gives you some tips how you should develop your SCD4J project 
* [FAQ](https://github.com/scd4j/gradle-plugins/wiki/10.-FAQ)
    * Frequent questions and answers
* [SCD4J Examples](https://github.com/scd4j/gradle-plugins/wiki/11.-SCD4J-Examples)
    * Describes, in a high level, each example in [scd4j-examples](https://github.com/scd4j/gradle-plugins/tree/master/scd4j-examples)

In short you must create a file `build.gradle` like the below example:

```
plugins {
    id "com.datamaio.scd4j" version "0.6.0"
}

scd4j {
    install {
        modules "my_module_dir"         // should be any dir into modules dir
        config  "my_config_file.conf"	// should be any properties file into config dir.
    }
}
```

Then you must create a directory `config` and another `modules`. Inside modules, you can create as much directories as many different modules you would like, in the above example we have defined a module `my_module_dir`, so you must have an dir `modules/my_module_dir`. In `config` dir you will put variables (in a properties like) that will be used in your modules. In our example, we have created an file with name `my_config_file.conf`. Then the directory structure of our SCD4J project we have:

```
build.gradle
config/
      my_config_file.conf
modules/
      my_module_dir/
                 ...
```

For more details of how to create an module, take a look at [Basics](https://github.com/scd4j/gradle-plugins/wiki/02.-Basics)
