package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.RemoteCaller;
import io.effi.rpc.contract.RemoteService;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitor Manager.
 */
public class MonitorManager {

    private List<RemoteService<?>> remoteServices = new ArrayList<>();

    private List<RemoteCaller<?>> remoteCallers = new ArrayList<>();

}
