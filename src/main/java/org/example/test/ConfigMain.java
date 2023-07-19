package org.example.test;

import org.example.annotation.ComponentScan;
import org.example.annotation.Import;
import org.example.test2.ContextT4;

@ComponentScan
@Import({ConfigT1.class,ConfigT2.class, ContextT4.class})
public class ConfigMain {
}
