package org.example.proxy.socks;

import org.example.proxy.socks.constants.AuthMethod;
import org.example.proxy.socks.constants.Version;

public class SocksSettings {
    private Version version;
    private AuthMethod authMethod;
    private EndPoint endPoint;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public String toString() {
        return "====================================\n" +
                "Version: " + version + "\n" +
                "Authentication Method: " + authMethod + "\n" +
                "Remote Address: " + endPoint.getAddress().toString() + "\n" +
                "Port: " + endPoint.getPort() + "\n" +
                "====================================";
    }
}
