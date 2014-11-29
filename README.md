SCD4J stands for Simple Continuous Delivery for Java and Groovy Developer. 
=============

In short, it is a set of tools being developed to automate the whole development cycle (from dev to production including builds, configurations, deployments, monitoring, etc) mainly focused for enterprises which opt to use Java technologies. 

More detail is comming soon. Stay tuned!

gradle-plugins Repository
=========================

Is the place were you going to find the gradle related source code used of these tools


STILL UNSTABLE
=========================
Use by your own risk
- Ubuntu the only OS which we have tested so far. 
- We are planning to improve support for CentOS/RedHat and, most of the functionalities, for Windows

Config Example:
```
plugins {
  id "com.datamaio.scd4j" version "0.1.7"
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

