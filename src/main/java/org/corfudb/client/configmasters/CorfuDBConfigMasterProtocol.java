/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.corfudb.client.configmasters;

import org.corfudb.client.IServerProtocol;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.corfudb.client.NetworkException;
import org.corfudb.client.UnwrittenException;
import org.corfudb.client.TrimmedException;
import org.corfudb.client.OverwriteException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.thetransactioncompany.jsonrpc2.client.*;
import com.thetransactioncompany.jsonrpc2.*;
import java.net.*;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.JsonString;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import javax.json.JsonReader;

import java.io.StringReader;

public class CorfuDBConfigMasterProtocol implements IServerProtocol, IConfigMaster
{
    private String host;
    private Integer port;
    private Map<String,String> options;
    private long epoch;
    private Logger log = LoggerFactory.getLogger(CorfuDBConfigMasterProtocol.class);
    private JSONRPC2Session jsonSession;
    private AtomicInteger id;

     public static String getProtocolString()
    {
        return "cdbcm";
    }

    public Integer getPort()
    {
        return port;
    }

    public String getHost()
    {
        return host;
    }

    public Map<String,String> getOptions()
    {
        return options;
    }

    public static IServerProtocol protocolFactory(String host, Integer port, Map<String,String> options, Long epoch)
    {
        return new CorfuDBConfigMasterProtocol(host, port, options, epoch);
    }

    public CorfuDBConfigMasterProtocol(String host, Integer port, Map<String,String> options, Long epoch)
    {
        this.host = host;
        this.port = port;
        this.options = options;
        this.epoch = epoch;
        this.id = new AtomicInteger();

        try
        {
            jsonSession = new JSONRPC2Session(new URL("http://"+ host + ":" + port + "/control"));
        }
        catch (Exception ex)
        {
            log.warn("Failed to connect to endpoint " + getFullString());
            throw new RuntimeException("Failed to connect to endpoint");
        }

    }

    public boolean ping()
    {
        try
        {
            JSONRPC2Request jr = new JSONRPC2Request("ping", id.getAndIncrement());
            JSONRPC2Response jres = jsonSession.send(jr);
            if (jres.indicatesSuccess() && jres.getResult().equals("pong"))
            {
                return true;
            }
        }
        catch(Exception e)
        {
            return false;
        }

        return false;
    }

    public void reset(long epoch)
    {

    }

    public void setEpoch(long epoch)
    {

    }

    public streamInfo getStream(UUID streamID)
    {
        try {
            JSONRPC2Request jr = new JSONRPC2Request("getstream", id.getAndIncrement());
            Map<String, Object> params = new HashMap<String,Object>();
            params.put("streamid", streamID.toString());
            jr.setNamedParams(params);
            JSONRPC2Response jres = jsonSession.send(jr);
            if (jres.indicatesSuccess())
            {
                Map<String, Object> jo = (Map<String,Object>) jres.getResult();
                        if ((Boolean) jo.get("present"))
                        {
                            streamInfo si = new streamInfo();
                            si.currentLog = UUID.fromString((String) jo.get("logid"));
                            si.startPos = (Long) jo.get("startpos");
                            return si;
                        }
                        else
                        {
                            return null;
                        }
            }
        } catch(Exception e) {
            log.error("other error", e);
            return null;
        }
        return null;
    }

    public boolean addStream(UUID logID, UUID streamID, long pos)
    {
        try {
            JSONRPC2Request jr = new JSONRPC2Request("addstream", id.getAndIncrement());
            Map<String, Object> params = new HashMap<String,Object>();
            params.put("logid", logID.toString());
            params.put("streamid", streamID.toString());
            params.put("startpos", pos);
            jr.setNamedParams(params);
            JSONRPC2Response jres = jsonSession.send(jr);
            if (jres.indicatesSuccess() && (Boolean) jres.getResult())
            {
                return true;
            }
            return false;
        } catch(Exception e) {
            return false;
        }
    }

    public boolean addLog(UUID logID, String path)
    {
        return true;
    }

    public String getLog(UUID logID)
    {
        return "";
    }

    public void resetAll()
    {
        try {
            JSONRPC2Request jr = new JSONRPC2Request("reset", id.getAndIncrement());
            JSONRPC2Response jres = jsonSession.send(jr);
            if (jres.indicatesSuccess() && (Boolean) jres.getResult())
            {
            }
        } catch(Exception e) {
        }

    }

}
