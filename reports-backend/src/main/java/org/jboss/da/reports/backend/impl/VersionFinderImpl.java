package org.jboss.da.reports.backend.impl;

import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.api.AproxConnector;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.reports.backend.api.VersionFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class VersionFinderImpl implements VersionFinder {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private AproxConnector aproxConnector;

    @Override
    public List<String> getVersionsFor(GAV gav) throws CommunicationException {
        return aproxConnector.getVersionsOfGA(gav.getGa());
    }

    @Override
    public String getBestMatchVersionFor(GAV gav) throws CommunicationException {
        List<String> obtainedVersions = aproxConnector.getVersionsOfGA(gav.getGa());
        return findBiggestMatchingVersion(gav, obtainedVersions);
    }

    private String findBiggestMatchingVersion(GAV gav, List<String> obtainedVersions) {
        String bestMatchVersion = null;
        int biggestBuildNumber = 0;

        String origVersion = gav.getVersion();
        Pattern pattern = Pattern.compile(origVersion + ".*\\.redhat-(\\d+)\\D*");

        for (String ver : obtainedVersions) {
            Matcher matcher = pattern.matcher(ver);
            if (matcher.matches()) {
                int foundBuildNumber = Integer.parseInt(matcher.group(1));
                if (foundBuildNumber > biggestBuildNumber) {
                    bestMatchVersion = ver;
                    biggestBuildNumber = foundBuildNumber;
                }
            }
        }
        return bestMatchVersion;
    }

}
