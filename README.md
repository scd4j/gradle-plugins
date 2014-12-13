SCD4J stands for Simple Continuous Delivery for Java and Groovy Developer. 
=============

In short, SCD4J is an automation platform for configuring and installing your Web IT infrastructure. With just one command you can install clusters and deploy applications. Note taht SCD4J is not another option for Puppet or Chef. It is, actually, a SIMPLER option than those tools.

For more details and advantages see documentation at [wiki](https://github.com/scd4j/gradle-plugins/wiki )

Config Example:
```
plugins {
  id "com.datamaio.scd4j" version "0.3"
}

dependencies {
	// dependencies we desire to install - Must be in your own repo (my_repo in this case)
	scd4j url('http://download.jboss.org/wildfly/8.2.0.Final/wildfly-8.2.0.Final.zip')
}

scd4j {
	install {
		modules = ["my_module_dir"] 	// should be any dir into modules dir
		config = "my_config_file.conf"	// should be any property file into config dir. By convention we strongly suggest to put the extention .conf in the file
		env {
			production = ["192.168.10.21"]		  	  	  // your production ips
			staging    = ["192.168.10.21"]		  	  	  // your staging ips
			testing    = ["192.168.7.20", "192.168.7.21"]  // your test/stage ips
		}
	}
}
```

