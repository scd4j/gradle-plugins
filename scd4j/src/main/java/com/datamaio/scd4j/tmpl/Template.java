package com.datamaio.scd4j.tmpl;

import java.util.Map;

public interface Template {
    Writable make();
    Writable make(Map<String, ? extends Object> binding);
}
