(function (jQuery) { // this closure helps us keep our variables to ourselves.
    // This pattern is known as an "iife" - immediately invoked function expression
    // form the URL
    var pluginUrl = AJS.contextPath() + "/rest/mathjax/1.0/admin/";

    // Lookup table of known CDNs
    // We store the URL in two parts, one to the left of the version, one to the right
    // This is so we can do: downloadUrl_a + $url + downloadUrl_b to get a final url
    // The version key is just the field in the JSON that has the value for version.
    // Version fetch is the api url that will give us what versions of the javascript are available.
    var cdnList = {
        'cloudflare': {
            downloadUrl_a: "https://cdnjs.cloudflare.com/ajax/libs/mathjax/",
            downloadUrl_b: "/MathJax.js?config=TeX-MML-AM_CHTML",
            versionFetch: "https://api.cdnjs.com/libraries/mathjax",
            versionKey: "version"
        },
        'rawgit': {
            downloadUrl_a: "https://cdn.rawgit.com/mathjax/MathJax/",
            downloadUrl_b: "/MathJax.js",
            versionFetch: "https://api.github.com/repos/mathjax/MathJax/releases",
            versionKey: "tag_name"
        },
        'jsdelivr': {
            downloadUrl_a: "https://cdn.jsdelivr.net/mathjax/",
            downloadUrl_b: "/MathJax.js",
            versionFetch: "https://api.jsdelivr.com/v1/jsdelivr/libraries?name=mathjax",
            versionKey: "versions"
        }
    };

    // Gets the url currently stored in Confluence's REST API, within the route for configuring MathJax
    var getActiveUrl = function () {
        // request the config information from the server
        jQuery.ajax({
            url: pluginUrl,
            dataType: "json"
        }).done(function (config) { // when the configuration is returned...
            // ...populate the form.u
            jQuery("#url").val(config.url);
            jQuery("#serverUrl").val(config.serverUrl);
            jQuery("#url-info").text(config.url);
            jQuery("#inlineMathjaxStartIdentifier").val(config.inlineMathjaxStartIdentifier);
            jQuery("#inlineMathjaxEndIdentifier").val(config.inlineMathjaxEndIdentifier);
            jQuery("#blockMathjaxStartIdentifier").val(config.blockMathjaxStartIdentifier);
            jQuery("#blockMathjaxEndIdentifier").val(config.blockMathjaxEndIdentifier);
            jQuery("#mathjaxAsciiMathStartIdentifier").val(config.mathjaxAsciiMathStartIdentifier);
            jQuery("#mathjaxAsciiMathEndIdentifier").val(config.mathjaxAsciiMathEndIdentifier);
        });
    }

    // wait for the DOM (i.e., document "skeleton") to load. Needs to happen for listeners to attach
    jQuery(document).ready(function () {
        var cdnMeta = {};
        getActiveUrl();

        // If the URL gen is submitted we populate the URL field with the resulting URL
        jQuery("#urlGen").submit(function (e) {
            e.preventDefault();

            // Weird URL construction, but it works
            jQuery("#url").val(cdnMeta.downloadUrl_a + jQuery("#select-version option:selected").val() + cdnMeta.downloadUrl_b);
        });

        jQuery("#admin").submit(function (e) {
            e.preventDefault();
            updateConfig();
        });

        // Use the selected CDN to fetch version numbers from that CDN's api
        var fetchVersion = function () {
            var selectedCDN = jQuery("#select-cdn option:selected").val();
            cdnMeta = cdnList[selectedCDN];
            jQuery.ajax({
                url: cdnMeta.versionFetch,
                dataType: "json"
            })
                .done(function (res) {
                    var versions = [];
                    if (selectedCDN == 'jsdelivr') {
                        versions = res[0][cdnMeta.versionKey];
                    } else if (selectedCDN == 'rawgit') {
                        versions = res.map(function (release) {
                            return release[cdnMeta.versionKey]
                        })
                    } else {
                        versions = res.assets.map(function (release) {
                            return release[cdnMeta.versionKey]
                        })
                    }

                    console.log(versions);
                    var versionSelect = jQuery('#select-version');
                    // Clear all options then repopulate
                    versionSelect.html("");
                    versions.forEach(function (version) {
                        versionSelect.append(jQuery("<option />").val(version).text(version));
                    })
                })
        }; fetchVersion();

        var apiSelect = jQuery("#select-cdn");
        apiSelect.change(fetchVersion);
    });

    // Goes to Confluence REST API and updates the stored configuration
    // Also edits the message on the page to indicate whether the operation succeeded or not
    function updateConfig() {
        jQuery.ajax({
            url: pluginUrl,
            type: "PUT",
            contentType: "application/json",
            data: '{ "url": "' + jQuery("#url").attr("value") + '"' + ', '
                + '"serverUrl": "' + jQuery("#serverUrl").attr("value") + '"' + ', '
                + '"inlineMathjaxStartIdentifier": "' + jQuery("#inlineMathjaxStartIdentifier").attr("value").replace("\\", "\\\\") + '"' + ", "
                + '"inlineMathjaxEndIdentifier": "' + jQuery("#inlineMathjaxEndIdentifier").attr("value").replace("\\", "\\\\") + '"' + ", "
                + '"blockMathjaxStartIdentifier": "' + jQuery("#blockMathjaxStartIdentifier").attr("value").replace("\\", "\\\\") + '"' + ", "
                + '"blockMathjaxEndIdentifier": "' + jQuery("#blockMathjaxEndIdentifier").attr("value").replace("\\", "\\\\") + '"' + ", "
                + '"mathjaxAsciiMathStartIdentifier": "' + jQuery("#mathjaxAsciiMathStartIdentifier").attr("value").replace("\\", "\\\\") + '"' + ", "
                + '"mathjaxAsciiMathEndIdentifier": "' + jQuery("#mathjaxAsciiMathEndIdentifier").attr("value").replace("\\", "\\\\") + '"'
                + '}',
            processData: false
        })
            .done(function () {
                getActiveUrl();
                jQuery("#message").text("Update Succeeded");
            })
            .fail(function (res) {
                console.error(res);
                jQuery("#message").text("Update Failed");
            })
    }
})(jQuery);
