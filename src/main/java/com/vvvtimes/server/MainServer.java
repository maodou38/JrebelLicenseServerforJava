package com.vvvtimes.server;

import com.vvvtimes.JrebelUtil.JrebelSign;
import com.vvvtimes.util.rsasign;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class MainServer extends AbstractHandler {

    private static Map<String, String> parseArguments(String[] args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Error in argument's length ");
        }

        Map<String, String> params = new HashMap<String, String>();

        for (int i = 0, len = args.length; i < len; ) {
            String argName = args[i++];

            if (argName.charAt(0) == '-') {
                if (argName.length() < 2) {
                    throw new IllegalArgumentException("Error at argument " + argName);
                }

                argName = argName.substring(1);
            }

            params.put(argName, args[i++]);
        }

        return params;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> arguments = parseArguments(args);
        String port = arguments.get("p");

        if (port == null || !port.matches("\\d+")) {
            port = "8081";
        }

        Server server = new Server(Integer.parseInt(port));
        server.setHandler(new MainServer());
        server.start();

        System.out.println("License Server started at http://localhost:" + port);
        System.out.println("JetBrains Activation address was: http://localhost:" + port + "/");
        System.out.println("JRebel 7.1 and earlier version Activation address was: http://localhost:" + port + "/{tokenname}, with any email.");
        System.out.println("JRebel 2018.1 and later version Activation address was: http://localhost:" + port + "/{guid}(eg:http://localhost:" + port + "/" + UUID.randomUUID().toString() + "), with any email.");

        server.join();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println(target);
        if (target.equals("/")) {
            indexHandler(target, baseRequest, request, response);
        } else if (target.equals("/jrebel/leases")) {
            jrebelLeasesHandler(target, baseRequest, request, response);
        } else if (target.equals("/jrebel/leases/1")) {
            jrebelLeases1Handler(target, baseRequest, request, response);
        } else if (target.equals("/agent/leases")) {
            jrebelLeasesHandler(target, baseRequest, request, response);
        } else if (target.equals("/agent/leases/1")) {
            jrebelLeases1Handler(target, baseRequest, request, response);
        } else if (target.equals("/jrebel/validate-connection")) {
            jrebelValidateHandler(target, baseRequest, request, response);
        } else if (target.equals("/rpc/ping.action")) {
            pingHandler(target, baseRequest, request, response);
        } else if (target.equals("/rpc/obtainTicket.action")) {
            obtainTicketHandler(target, baseRequest, request, response);
        } else if (target.equals("/rpc/releaseTicket.action")) {
            releaseTicketHandler(target, baseRequest, request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void jrebelValidateHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        String jsonStr = "{\n" +
                "    \"serverVersion\": \"3.2.4\",\n" +
                "    \"serverProtocolVersion\": \"1.1\",\n" +
                "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" +
                "    \"groupType\": \"managed\",\n" +
                "    \"statusCode\": \"SUCCESS\",\n" +
                "    \"company\": \"Administrator\",\n" +
                "    \"canGetLease\": true,\n" +
                "    \"licenseType\": 1,\n" +
                "    \"evaluationLicense\": false,\n" +
                "    \"seatPoolType\": \"standalone\"\n" +
                "}\n";
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        String body = jsonObject.toString();
        response.getWriter().print(body);
    }

    private void jrebelLeases1Handler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String username = request.getParameter("username");
        baseRequest.setHandled(true);
        String jsonStr = "{\n" +
                "    \"serverVersion\": \"3.2.4\",\n" +
                "    \"serverProtocolVersion\": \"1.1\",\n" +
                "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" +
                "    \"groupType\": \"managed\",\n" +
                "    \"statusCode\": \"SUCCESS\",\n" +
                "    \"msg\": null,\n" +
                "    \"statusMessage\": null\n" +
                "}\n";
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        if (username != null) {
            jsonObject.put("company", username);
        }
        String body = jsonObject.toString();
        response.getWriter().print(body);

    }

    private void jrebelLeasesHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String clientRandomness = request.getParameter("randomness");
        String username = request.getParameter("username");
        String guid = request.getParameter("guid");
        System.out.println(((Request) request).getParameters());
        boolean offline = Boolean.parseBoolean(request.getParameter("offline"));
        String validFrom = "null";
        String validUntil = "null";
        if (offline) {
            String clientTime = request.getParameter("clientTime");
            String offlineDays = request.getParameter("offlineDays");
            //long clinetTimeUntil = Long.parseLong(clientTime) + Long.parseLong(offlineDays)  * 24 * 60 * 60 * 1000;
            long clinetTimeUntil = Long.parseLong(clientTime) + 180L * 24 * 60 * 60 * 1000;
            validFrom = clientTime;
            validUntil = String.valueOf(clinetTimeUntil);
        }
        baseRequest.setHandled(true);
        String jsonStr = "{\n" +
                "    \"serverVersion\": \"3.2.4\",\n" +
                "    \"serverProtocolVersion\": \"1.1\",\n" +
                "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" +
                "    \"groupType\": \"managed\",\n" +
                "    \"id\": 1,\n" +
                "    \"licenseType\": 1,\n" +
                "    \"evaluationLicense\": false,\n" +
                "    \"signature\": \"OJE9wGg2xncSb+VgnYT+9HGCFaLOk28tneMFhCbpVMKoC/Iq4LuaDKPirBjG4o394/UjCDGgTBpIrzcXNPdVxVr8PnQzpy7ZSToGO8wv/KIWZT9/ba7bDbA8/RZ4B37YkCeXhjaixpmoyz/CIZMnei4q7oWR7DYUOlOcEWDQhiY=\",\n" +
                "    \"serverRandomness\": \"H2ulzLlh7E0=\",\n" +
                "    \"seatPoolType\": \"standalone\",\n" +
                "    \"statusCode\": \"SUCCESS\",\n" +
                "    \"offline\": " + String.valueOf(offline) + ",\n" +
                "    \"validFrom\": " + validFrom + ",\n" +
                "    \"validUntil\": " + validUntil + ",\n" +
                "    \"company\": \"Administrator\",\n" +
                "    \"orderId\": \"\",\n" +
                "    \"zeroIds\": [\n" +
                "        \n" +
                "    ],\n" +
                "    \"licenseValidFrom\": 1490544001000,\n" +
                "    \"licenseValidUntil\": 1691839999000\n" +
                "}";

        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        if (clientRandomness == null || username == null || guid == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            JrebelSign jrebelSign = new JrebelSign();
            jrebelSign.toLeaseCreateJson(clientRandomness, guid, offline, validFrom, validUntil);
            String signature = jrebelSign.getSignature();
            jsonObject.put("signature", signature);
            jsonObject.put("company", username);
            String body = jsonObject.toString();
            response.getWriter().print(body);
        }
    }

    private void releaseTicketHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String salt = request.getParameter("salt");
        baseRequest.setHandled(true);
        if (salt == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            String xmlContent = "<ReleaseTicketResponse><message></message><responseCode>OK</responseCode><salt>" + salt + "</salt></ReleaseTicketResponse>";
            String xmlSignature = rsasign.Sign(xmlContent);
            String body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
            response.getWriter().print(body);
        }
    }

    private void obtainTicketHandler(String target, Request baseRequest, HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        SimpleDateFormat fm = new SimpleDateFormat("EEE,d MMM yyyy hh:mm:ss Z", Locale.ENGLISH);
        String date = fm.format(new Date()) + " GMT";
        //response.setHeader("Date", date);
        //response.setHeader("Server", "fasthttp");
        String salt = request.getParameter("salt");
        String username = request.getParameter("userName");
        String prolongationPeriod = "607875500";
        baseRequest.setHandled(true);
        if (salt == null || username == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            String xmlContent = "<ObtainTicketResponse><message></message><prolongationPeriod>" + prolongationPeriod + "</prolongationPeriod><responseCode>OK</responseCode><salt>" + salt + "</salt><ticketId>1</ticketId><ticketProperties>licensee=" + username + "\tlicenseType=0\t</ticketProperties></ObtainTicketResponse>";
            String xmlSignature = rsasign.Sign(xmlContent);
            String body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
            response.getWriter().print(body);
        }
    }

    private void pingHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String salt = request.getParameter("salt");
        baseRequest.setHandled(true);
        if (salt == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            String xmlContent = "<PingResponse><message></message><responseCode>OK</responseCode><salt>" + salt + "</salt></PingResponse>";
            String xmlSignature = rsasign.Sign(xmlContent);
            String body = "<!-- " + xmlSignature + " -->\n" + xmlContent;
            response.getWriter().print(body);
        }

    }

    private void indexHandler(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        // 拼接服务器地址
        String licenseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        StringBuffer html = new StringBuffer("<h3>使用说明（Instructions for use）</h3>");

        html.append("<hr/>");

        html.append("<h1>Hello,This is a Jrebel & JetBrains License Server!</h1>");
        html.append("<p>License Server started at ").append(licenseUrl);
        html.append("<p>JetBrains Activation address was: <span style='color:red'>").append(licenseUrl).append("/");
        html.append("<p>JRebel 7.1 and earlier version Activation address was: <span style='color:red'>")
                .append(licenseUrl).append("/{tokenname}")
                .append("</span>, with any email.");
        html.append("<p>JRebel 2018.1 and later version Activation address was: ")
                .append(licenseUrl).append("/{guid}")
                .append("(eg:<span style='color:red'>")
                .append(licenseUrl).append("/").append(UUID.randomUUID().toString())
                .append("</span>), with any email.");

        html.append("<hr/>");

        html.append("<h1>Hello，此地址是 Jrebel & JetBrains License Server!</h1>");
        html.append("<p>JetBrains许可服务器激活地址 ").append(licenseUrl);
        html.append("<p>JetBrains激活地址是: <span style='color:red'>").append(licenseUrl).append("/");
        html.append("<p>JRebel 7.1 及旧版本激活地址: <span style='color:red'>")
                .append(licenseUrl).append("/{tokenname}")
                .append("</span>, 以及任意邮箱地址。");
        html.append("<p>JRebel 2018.1+ 版本激活地址: ")
                .append(licenseUrl).append("/{guid}")
                .append("(例如：<span style='color:red'>")
                .append(licenseUrl).append("/").append(UUID.randomUUID().toString())
                .append("</span>), 以及任意邮箱地址。");

        response.getWriter().println(html);
    }
}