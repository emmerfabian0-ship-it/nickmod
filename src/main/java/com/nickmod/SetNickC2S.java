package com.nickmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> Server: "das ist mein Nickname". */
public record SetNickC2S(NickData data) implements CustomPacketPayload {
    public static final Type<SetNickC2S> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("nickmod", "set_nick"));
    public static final StreamCodec<ByteBuf, SetNickC2S> CODEC =
            NickData.CODEC.map(SetNickC2S::new, SetNickC2S::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
