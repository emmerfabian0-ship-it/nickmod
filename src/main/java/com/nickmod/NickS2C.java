package com.nickmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/** Server -> Client: "Spieler X heisst jetzt so". enabled=false bedeutet: Nick entfernen. */
public record NickS2C(UUID uuid, NickData data) implements CustomPacketPayload {
    public static final Type<NickS2C> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("nickmod", "nick_sync"));
    public static final StreamCodec<ByteBuf, NickS2C> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NickS2C::uuid,
            NickData.CODEC, NickS2C::data,
            NickS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
