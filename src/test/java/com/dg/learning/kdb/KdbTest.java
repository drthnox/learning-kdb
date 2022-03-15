package com.dg.learning.kdb;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources"},
        glue = {"com.dg.learning.kdb.glue"},
        tags = {"~@ignore"}
)
public class KdbTest {
    @BeforeClass
    public static void beforeClass() {

    }

    @AfterClass
    public static void afterClass() {
    }
}
