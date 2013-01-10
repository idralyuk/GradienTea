<%--@ page import="com.cengage.analytics.reports.mindtap.ui.server.GoogleAnalyticsData" --%>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.InputStream" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="gwt:property" content="baseUrl=GradienTeaUI/">

    <title>GradienTea Dome Pattern Designer</title>
    <script src="js/three.min.js" type="text/javascript"></script>
    <%
        String appName = "GradienTeaUI";

        boolean needsScriptImport = true;

        // Only inline the javascript if we're not in development mode
        if (request.getParameter("gwt.codesvr") == null) {
            try {
                // Try to load the nocache javascript
                InputStream inputStream =
                        application.getResourceAsStream("/" + appName + "/" + appName + ".nocache.js");
                if (inputStream == null)
                    throw new IOException("Unable to find nocache.js file");

                // Success. Write out the script, escaping end-script tags
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                out.write("<script type=\"text/javascript\" language=\"javascript\">");
                for (String line; (line = reader.readLine()) != null; ) {
                    out.write(line.replaceAll("</script", "</scr'+'ipt") + "\r\n");
                }
                out.write("</script>");

                needsScriptImport = false;
            } catch (IOException e) { }
        }

        if (needsScriptImport) {
            // Write out the standard script import if in dev mode or if loading the script failed
            out.write("<script type=\"text/javascript\" language=\"javascript\" src=\""+appName+"/"+appName+".nocache.js?r=" + Math.random() + "\"></script>");
        }
    %>

    <style type="text/css">
        body {
            background-color: black;
        }
    </style>

</head>
<body class="gradientea">

<noscript>
    <h3>The GradienTea Pattern Designer Requires Javascript!</h3>

    Your browser either does not support, or has disabled javascript. Please enable javascript and refresh this page, or
    use a browser which supports Javascript to view this application.
</noscript>
</body>
</html>
