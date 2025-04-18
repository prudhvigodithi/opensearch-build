#!/bin/bash

# Copyright OpenSearch Contributors
# SPDX-License-Identifier: Apache-2.0

#
# Run OpenSearch-Dashboards, using environment variables to
# set longopts defining OpenSearch-Dashboards's configuration.
#
# eg. Setting the environment variable:
#
#       OPENSEARCH_STARTUPTIMEOUT=60
#
# will cause OpenSearch-Dashboards to be invoked with:
#
#       --opensearch.startupTimeout=60

# Setup Home Directory
export OPENSEARCH_DASHBOARDS_HOME=/usr/share/opensearch-dashboards

opensearch_dashboards_vars=(
    console.enabled
    console.proxyConfig
    console.proxyFilter
    ops.cGroupOverrides.cpuPath
    ops.cGroupOverrides.cpuAcctPath
    cpu.cgroup.path.override
    cpuacct.cgroup.path.override
    csp.rules
    csp.strict
    csp.warnLegacyBrowsers
    data.search.usageTelemetry.enabled
    opensearch.customHeaders
    opensearch.hosts
    opensearch.logQueries
    opensearch.memoryCircuitBreaker.enabled
    opensearch.memoryCircuitBreaker.maxPercentage
    opensearch.password
    opensearch.pingTimeout
    opensearch.requestHeadersWhitelist
    opensearch.requestTimeout
    opensearch.shardTimeout
    opensearch.sniffInterval
    opensearch.sniffOnConnectionFault
    opensearch.sniffOnStart
    opensearch.ssl.alwaysPresentCertificate
    opensearch.ssl.certificate
    opensearch.ssl.certificateAuthorities
    opensearch.ssl.key
    opensearch.ssl.keyPassphrase
    opensearch.ssl.keystore.path
    opensearch.ssl.keystore.password
    opensearch.ssl.truststore.path
    opensearch.ssl.truststore.password
    opensearch.ssl.verificationMode
    opensearch.username
    i18n.locale
    interpreter.enableInVisualize
    opensearchDashboards.autocompleteTerminateAfter
    opensearchDashboards.autocompleteTimeout
    opensearchDashboards.defaultAppId
    opensearchDashboards.index
    logging.dest
    logging.json
    logging.quiet
    logging.rotate.enabled
    logging.rotate.everyBytes
    logging.rotate.keepFiles
    logging.rotate.pollingInterval
    logging.rotate.usePolling
    logging.silent
    logging.useUTC
    logging.verbose
    map.includeOpenSearchMapsService
    map.proxyOpenSearchMapsServiceInMaps
    map.regionmap
    map.tilemap.options.attribution
    map.tilemap.options.maxZoom
    map.tilemap.options.minZoom
    map.tilemap.options.subdomains
    map.tilemap.url
    monitoring.cluster_alerts.email_notifications.email_address
    monitoring.enabled
    monitoring.opensearchDashboards.collection.enabled
    monitoring.opensearchDashboards.collection.interval
    monitoring.ui.container.opensearch.enabled
    monitoring.ui.container.logstash.enabled
    monitoring.ui.opensearch.password
    monitoring.ui.opensearch.pingTimeout
    monitoring.ui.opensearch.hosts
    monitoring.ui.opensearch.username
    monitoring.ui.opensearch.logFetchCount
    monitoring.ui.opensearch.ssl.certificateAuthorities
    monitoring.ui.opensearch.ssl.verificationMode
    monitoring.ui.enabled
    monitoring.ui.max_bucket_size
    monitoring.ui.min_interval_seconds
    newsfeed.enabled
    ops.interval
    path.data
    pid.file
    regionmap
    security.showInsecureClusterWarning
    server.basePath
    server.customResponseHeaders
    server.compression.enabled
    server.compression.referrerWhitelist
    server.cors
    server.cors.origin
    server.defaultRoute
    server.host
    server.keepAliveTimeout
    server.maxPayloadBytes
    server.name
    server.port
    server.rewriteBasePath
    server.socketTimeout
    server.ssl.cert
    server.ssl.certificate
    server.ssl.certificateAuthorities
    server.ssl.cipherSuites
    server.ssl.clientAuthentication
    server.customResponseHeaders
    server.ssl.enabled
    server.ssl.key
    server.ssl.keyPassphrase
    server.ssl.keystore.path
    server.ssl.keystore.password
    server.ssl.truststore.path
    server.ssl.truststore.password
    server.ssl.redirectHttpFromPort
    server.ssl.supportedProtocols
    server.xsrf.disableProtection
    server.xsrf.whitelist
    status.allowAnonymous
    status.v6ApiFormat
    tilemap.options.attribution
    tilemap.options.maxZoom
    tilemap.options.minZoom
    tilemap.options.subdomains
    tilemap.url
    timeline.enabled
    vega.enableExternalUrls
    apm_oss.apmAgentConfigurationIndex
    apm_oss.indexPattern
    apm_oss.errorIndices
    apm_oss.onboardingIndices
    apm_oss.spanIndices
    apm_oss.sourcemapIndices
    apm_oss.transactionIndices
    apm_oss.metricsIndices
    telemetry.allowChangingOptInStatus
    telemetry.enabled
    telemetry.optIn
    telemetry.optInStatusUrl
    telemetry.sendUsageFrom
)

function setupSecurityDashboardsPlugin {
    SECURITY_DASHBOARDS_PLUGIN="securityDashboards"

    if [ -d "$OPENSEARCH_DASHBOARDS_HOME/plugins/$SECURITY_DASHBOARDS_PLUGIN" ]; then
        if [ "$DISABLE_SECURITY_DASHBOARDS_PLUGIN" = "true" ]; then
            echo "Disabling OpenSearch Security Dashboards Plugin"
            ./bin/opensearch-dashboards-plugin remove securityDashboards

            # Remove all security related parameters as well as changing HTTPS to HTTP
            # Temporary fix before security-dashboards plugin implement a parameter to disable the plugin entirely
            # https://github.com/opensearch-project/security-dashboards-plugin/issues/896
            UPDATED_CONFIG=`cat $OPENSEARCH_DASHBOARDS_HOME/config/opensearch_dashboards.yml | sed "/^opensearch_security/d" | sed "s/https/http/g"`
            echo "$UPDATED_CONFIG" > $OPENSEARCH_DASHBOARDS_HOME/config/opensearch_dashboards.yml
        fi
    fi
}

function runOpensearchDashboards {
    longopts=()
    for opensearch_dashboards_var in ${opensearch_dashboards_vars[*]}; do
        # 'opensearch.hosts' -> 'OPENSEARCH_URL'
        env_var=$(echo ${opensearch_dashboards_var^^} | tr . _)

        # Indirectly lookup env var values via the name of the var.
        # REF: http://tldp.org/LDP/abs/html/bashver2.html#EX78
        value=${!env_var}
        if [[ -n $value ]]; then
            longopt="--${opensearch_dashboards_var}=${value}"
            longopts+=("${longopt}")
        fi
    done

    # Files created at run-time should be group-writable, for Openshift's sake.
    umask 0002

    ##Security Dashboards Plugin
    setupSecurityDashboardsPlugin

    # TO DO:
    # Confirm with Mihir if this is necessary

    # The virtual file /proc/self/cgroup should list the current cgroup
    # membership. For each hierarchy, you can follow the cgroup path from
    # this file to the cgroup filesystem (usually /sys/fs/cgroup/) and
    # introspect the statistics for the cgroup for the given
    # hierarchy. Alas, Docker breaks this by mounting the container
    # statistics at the root while leaving the cgroup paths as the actual
    # paths. Therefore, OpenSearch-Dashboards provides a mechanism to override
    # reading the cgroup path from /proc/self/cgroup and instead uses the
    # cgroup path defined the configuration properties
    # cpu.cgroup.path.override and cpuacct.cgroup.path.override.
    # Therefore, we set this value here so that cgroup statistics are
    # available for the container this process will run in.

    exec "$@" \
        --ops.cGroupOverrides.cpuPath=/ \
        --ops.cGroupOverrides.cpuAcctPath=/ \
        "${longopts[@]}"
}

# Prepend "opensearch-dashboards" command if no argument was provided or if the
# first argument looks like a flag (i.e. starts with a dash).
if [ $# -eq 0 ] || [ "${1:0:1}" = '-' ]; then
    set -- opensearch-dashboards "$@"
fi

if [ "$1" = "opensearch-dashboards" ]; then
    runOpensearchDashboards "$@"
else
    exec "$@"
fi
