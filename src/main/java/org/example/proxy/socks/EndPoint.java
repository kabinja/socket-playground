package org.example.proxy.socks;

import org.example.proxy.socks.constants.AddressType;

import java.net.InetAddress;

public class EndPoint {
    private final AddressType addressType;
    private final byte[] addressBuffer;
    private final InetAddress address;
    private final int port;

    public EndPoint(AddressType addressType, byte[] addressBuffer, InetAddress address, int port) {
        this.addressType = addressType;
        this.addressBuffer = addressBuffer;
        this.address = address;
        this.port = port;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public byte[] getAddressBuffer() {
        return addressBuffer;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
