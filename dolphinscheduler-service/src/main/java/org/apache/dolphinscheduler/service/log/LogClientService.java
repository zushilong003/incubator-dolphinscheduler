/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.service.log;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.log.*;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Address;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * log client
 */
public class LogClientService {

    private static final Logger logger = LoggerFactory.getLogger(LogClientService.class);

    private final NettyClientConfig clientConfig;

    private final NettyRemotingClient client;

    /**
     *  request time out
     */
    private final long logRequestTimeout = 10 * 1000;

    /**
     * construct client
     */
    public LogClientService() {
        this.clientConfig = new NettyClientConfig();
        this.clientConfig.setWorkerThreads(4);
        this.client = new NettyRemotingClient(clientConfig);
    }

    /**
     * close
     */
    public void close()  {
        this.client.close();
        logger.info("logger client closed");
    }

    /**
     * roll view log
     * @param host host
     * @param port port
     * @param path path
     * @param skipLineNum skip line number
     * @param limit limit
     * @return log content
     */
    public String rollViewLog(String host, int port, String path,int skipLineNum,int limit) {
        logger.info("roll view log, host : {}, port : {}, path {}, skipLineNum {} ,limit {}", host, port, path, skipLineNum, limit);
        RollViewLogRequestCommand request = new RollViewLogRequestCommand(path, skipLineNum, limit);
        String result = "";
        final Address address = new Address(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, logRequestTimeout);
            if(response != null){
                RollViewLogResponseCommand rollReviewLog = FastJsonSerializer.deserialize(
                        response.getBody(), RollViewLogResponseCommand.class);
                return rollReviewLog.getMsg();
            }
        } catch (Exception e) {
            logger.error("roll view log error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
    }

    /**
     * view log
     * @param host host
     * @param port port
     * @param path path
     * @return log content
     */
    public String viewLog(String host, int port, String path) {
        logger.info("view log path {}", path);
        ViewLogRequestCommand request = new ViewLogRequestCommand(path);
        String result = "";
        final Address address = new Address(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, logRequestTimeout);
            if(response != null){
                ViewLogResponseCommand viewLog = FastJsonSerializer.deserialize(
                        response.getBody(), ViewLogResponseCommand.class);
                return viewLog.getMsg();
            }
        } catch (Exception e) {
            logger.error("view log error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
    }

    /**
     * get log size
     * @param host host
     * @param port port
     * @param path log path
     * @return log content bytes
     */
    public byte[] getLogBytes(String host, int port, String path) {
        logger.info("log path {}", path);
        GetLogBytesRequestCommand request = new GetLogBytesRequestCommand(path);
        byte[] result = null;
        final Address address = new Address(host, port);
        try {
            Command command = request.convert2Command();
            Command response = this.client.sendSync(address, command, logRequestTimeout);
            if(response != null){
                GetLogBytesResponseCommand getLog = FastJsonSerializer.deserialize(
                        response.getBody(), GetLogBytesResponseCommand.class);
                return getLog.getData();
            }
        } catch (Exception e) {
            logger.error("get log size error", e);
        } finally {
            this.client.closeChannel(address);
        }
        return result;
    }
}