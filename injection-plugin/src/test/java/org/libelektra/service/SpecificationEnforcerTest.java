package org.libelektra.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.model.SpecificationDataResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class SpecificationEnforcerTest {

    private KeySet specification;
    private KeySet configKeySet;
    private Key changedKey;
    private Key specKey;

    private final String changedKeyPath = "user/config/test";
    private final String specKeyPath = "user/spec/test";

    private final String configPath = "user/config";
    private final String specPath = "user/spec";

    private SpecificationEnforcer specificationEnforcer;

    @Before
    public void setUp() {
        specificationEnforcer = new SpecificationEnforcer(specPath, configPath);
        changedKey = Key.create(changedKeyPath, "");
        specKey = Key.create(specKeyPath, "");
        specification = KeySet.create();
        configKeySet = KeySet.create();
        configKeySet.append(changedKey);
        specification.append(specKey);
    }

    // ********* TYPE ***********
    @Test
    public void wrongEnum_shouldFail() {
        changedKey.setString("d");
        specKey.setMeta("type", "enum");
        specKey.setMeta("check/enum", "#2");
        specKey.setMeta("check/enum/#0", "a");
        specKey.setMeta("check/enum/#1", "b");
        specKey.setMeta("check/enum/#2", "c");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(true));
        assertThat(result.getPlugin(), is(SpecificationEnforcer.SpecPlugins.TYPE));
        assertThat(result.getErrorMessage(), containsString("Allowed values: 'a' 'b' 'c'"));
    }

    @Test
    public void correctEnum_shouldNotFail() {
        changedKey.setString("c");
        specKey.setMeta("type", "enum");
        specKey.setMeta("check/enum", "#2");
        specKey.setMeta("check/enum/#0", "a");
        specKey.setMeta("check/enum/#1", "b");
        specKey.setMeta("check/enum/#2", "c");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(false));
    }

    // ********* PATH ***********
    @Test
    public void wrongPath_shouldFail() {
        changedKey.setString("/does/not/exist");
        specKey.setMeta("type", "string");
        specKey.setMeta("check/path", "");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(true));
        assertThat(result.getPlugin(), is(SpecificationEnforcer.SpecPlugins.PATH));
//        assertThat(result.getErrorMessage(), containsString("Allowed values: 'a' 'b' 'c'"));
    }

    @Test
    public void correctPath_shouldNotFail() {
        changedKey.setString("/tmp");
        specKey.setMeta("type", "string");
        specKey.setMeta("check/path", "");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(false));
    }

    // ********* NETWORK ***********
    @Test
    public void wrongPort_shouldFail() {
        changedKey.setInteger(65536);
        specKey.setMeta("check/port", "");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(true));
        assertThat(result.getPlugin(), is(SpecificationEnforcer.SpecPlugins.NETWORK));
        assertThat(result.getErrorMessage(), containsString("not within 0 - 65535"));
    }

    @Test
    public void correctPort_shouldNotFail() {
        changedKey.setInteger(65535);
        specKey.setMeta("check/port", "");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(false));
    }

    // ********* RANGE ***********
    @Test
    public void wrongRange_shouldFail() {
        changedKey.setInteger(150);
        specKey.setMeta("check/range", "0-100");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(true));
        assertThat(result.getPlugin(), is(SpecificationEnforcer.SpecPlugins.RANGE));
        assertThat(result.getErrorMessage(), containsString("not within range 0-100"));
    }

    @Test
    public void correctRange_shouldNotFail() {
        changedKey.setInteger(10);
        specKey.setMeta("check/range", "0-100");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(false));
    }

    // ********* VALIDATION ***********
    @Test
    public void wrongRegex_shouldFail() {
        changedKey.setString("20*3");
        specKey.setMeta("check/validation", "([1-9]+[0-9]*)x([1-9]+[0-9]*)");
        specKey.setMeta("check/validation/match", "LINE");
        specKey.setMeta("check/validation/message", "Not a valid size declaration. Examples: 20x4, 19x3, 40x150");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(true));
        assertThat(result.getPlugin(), is(SpecificationEnforcer.SpecPlugins.VALIDATION));
        assertThat(result.getErrorMessage(), containsString("Not a valid size declaration. Examples: 20x4, 19x3, 40x150"));
    }

    @Test
    public void correctRegex_shouldNotFail() {
        changedKey.setString("20*3");
        specKey.setMeta("check/validation", "([1-9]+[0-9]*)x([1-9]+[0-9]*)");
        specKey.setMeta("check/validation/match", "LINE");
        specKey.setMeta("check/validation/message", "Not a valid size declaration. Examples: 20x4, 19x3, 40x150");

        SpecificationDataResult result = specificationEnforcer.checkSpecification(specification, configKeySet,
                changedKey);

        assertThat(result.hasDetectedError(), is(false));
    }

    @After
    public void cleanUp() {
        changedKey = null;
        specification = null;
        configKeySet = null;
    }

}