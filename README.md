SCD4J stands for Simple Continuous Delivery for Java and Groovy Developer. 
=============

In short, SCD4J is an automation platform for configuring and installing your Web IT infrastructure. With just one command you can install clusters and deploy applications. Note taht SCD4J is not another option for Puppet or Chef. It is, actually, a SIMPLER option than those tools.

For more details and advantages see documentation at [wiki](https://github.com/scd4j/gradle-plugins/wiki )

Config Example:
```
plugins {
  id "com.datamaio.scd4j" version "0.5.1"
}

scd4j {
	install {
		modules "my_module_dir" 	    // should be any dir into modules dir
		config  "my_config_file.conf"	// should be any properties file into config dir.
	}
}
```

For more complex installation take a look at [wiki](https://github.com/scd4j/gradle-plugins/wiki/02.-Basics)

