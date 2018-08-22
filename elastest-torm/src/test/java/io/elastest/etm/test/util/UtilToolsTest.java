package io.elastest.etm.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.elastest.etm.utils.UtilTools;

@RunWith(JUnitPlatform.class)
public class UtilToolsTest {

    @Test
    public void testGetElasTestHostOnWin() {
        assertEquals("localhost", UtilTools.getElasTestHostOnWin());
    }

    @Test
    public void testGetDockerHostIpOnWin() {
        assertEquals("", UtilTools.getDockerHostIpOnWin());
    }

    @Test
    public void testGetHostIp() throws Exception {
        assertNotNull(UtilTools.getHostIp());
    }

    @Test
    public void testGetDockerHostIp() throws Exception {
        assertNotNull(UtilTools.getDockerHostIp());
    }

    @Test
    public void testGetMyIp() {
        assertNotEquals("", UtilTools.getMyIp());
    }

    @Test
    public void testConvertJsonString() {
        assertNotNull(UtilTools.convertJsonString(5, int.class));
    }

    @Test
    public void testGenerateUniqueId() {
        assertNotNull(UtilTools.generateUniqueId());
    }

    @Test
    public void testFindRandomOpenPort() {
        boolean works = true;
        try {
            UtilTools.findRandomOpenPort();
        } catch (IOException e) {
            works = false;
        }

        assertTrue(works);
    }

    @Test
    public void testDoPing() throws IOException {
        assertNotNull(UtilTools.doPing("localhost"));
    }

    @Test
    public void testCheckIfUrlIsUp() throws IOException {
        assertThrows(Exception.class, () -> {
            UtilTools.checkIfUrlIsUp("incorrectUrl");
        });
    }

}
