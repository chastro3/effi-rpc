/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.protocol.http.FutureBinder;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Handles HTTP/2 stream frame responses. This is a useful approach if you specifically want to check
 * the main HTTP/2 response DATA/HEADERs, but in this example it's used purely to see whether
 * our request (for a specific stream id) has had a final response (for that same stream id).
 */
@NativeConfig.Reflect(typeReached = Http2Protocol.class, queryAllPublicMethods = true)
@Sharable
public final class Http2ClientHandler extends FutureBinder {

    @Override
    protected SmartURL supports(Object msg) {
        if (msg instanceof HttpDuplexRequest request) {
            return request.url();
        }
        return null;
    }

    @Override
    protected void writeHttpRequest(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpDuplexRequest request) {
            Http2StreamFrame[] frames = H2Support.toHttp2StreamFrames(request);
            for (Http2StreamFrame frame : frames) {
                ctx.write(frame, ctx.newPromise());
            }
        }
    }

    @Override
    protected boolean readHttpResponse(ChannelHandlerContext ctx, Object msg, CallContext<Request, Caller<?>> context) throws Exception {
        Http2ResponseStream responseStream = null;
        if (msg instanceof Http2HeadersFrame headersFrame) {
            responseStream = H2Support.getOrCreateResponseStream(ctx, headersFrame.stream());
            responseStream.parseHeaderFrame(headersFrame);
        } else if (msg instanceof Http2DataFrame dataFrame) {
            responseStream = H2Support.getOrCreateResponseStream(ctx, dataFrame.stream());
            responseStream.parseDataFrame(dataFrame);
        }
        if (responseStream != null && responseStream.endStream()) {
            HttpResponse httpResponse = H2Support.fromHttp2ResponseStream(responseStream, ctx, context);
            ctx.fireChannelRead(httpResponse);
            H2Support.removeResponseStream(ctx, responseStream);
            return true;
        }
        return false;
    }
}
