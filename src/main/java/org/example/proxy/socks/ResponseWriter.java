package org.example.proxy.socks;

import org.example.proxy.Convertor;
import org.example.proxy.socks.constants.Misc;
import org.example.proxy.socks.constants.Reply;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseWriter {
    private ResponseWriter() {}

    public static void write(final OutputStream out, final Reply reply, final SocksSettings settings) throws IOException {
        final EndPoint endPoint = settings.getEndPoint();

        out.write(settings.getVersion().getCode());
        out.write(reply.getCode());
        out.write(Misc.RESERVED.getCode());

        out.write(endPoint.getAddressType().getCode());
        out.write(endPoint.getAddressBuffer());

        out.write(Convertor.toByteArray((short)endPoint.getPort()));
    }
}
