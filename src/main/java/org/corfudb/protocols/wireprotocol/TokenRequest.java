package org.corfudb.protocols.wireprotocol;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by mwei on 8/9/16.
 */
@Data
@AllArgsConstructor
public class TokenRequest implements ICorfuPayload<TokenRequest> {

    /** The number of tokens to request. 0 means to request the current token. */
    final Long numTokens;

    /** The streams which are affected by this token request. */
    final Set<UUID> streams;

    final Map<UUID, Long> useTheseBackpointers;

    final Boolean replexOverwrite;

    public TokenRequest(ByteBuf buf) {
        numTokens = ICorfuPayload.fromBuffer(buf, Long.class);
        if (ICorfuPayload.fromBuffer(buf, Boolean.class))
            streams = ICorfuPayload.setFromBuffer(buf, UUID.class);
        else streams = null;
        useTheseBackpointers = ICorfuPayload.mapFromBuffer(buf, UUID.class, Long.class);
        replexOverwrite = ICorfuPayload.fromBuffer(buf, Boolean.class);
    }

    @Override
    public void doSerialize(ByteBuf buf) {
        ICorfuPayload.serialize(buf, numTokens);
        ICorfuPayload.serialize(buf, streams != null);
        if (streams != null)
            ICorfuPayload.serialize(buf, streams);
        ICorfuPayload.serialize(buf, useTheseBackpointers);
        ICorfuPayload.serialize(buf, replexOverwrite);
    }
}
