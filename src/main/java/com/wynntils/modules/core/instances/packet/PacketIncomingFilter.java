/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.core.instances.packet;

import com.wynntils.ModCore;
import com.wynntils.core.events.custom.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraftforge.common.MinecraftForge;

public class PacketIncomingFilter extends ChannelInboundHandlerAdapter {

    private static Minecraft mc = Minecraft.getMinecraft();

    /**
     * Dispatch a packet incoming event to be checked before reaching the
     * interpreter
     *
     * @see PacketEvent for more information about these events
     *
     *
     * @param ctx The Channel Handler
     * @param msg The incoming Packet
     * @throws Exception If something fails (idk exactly, that was inherited)
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) return;

        PacketEvent.Incoming<? extends Packet<?>> event = new PacketEvent.Incoming<>((Packet<?>) msg, ModCore.mc().getConnection(), this, ctx);
        boolean cancel = MinecraftForge.EVENT_BUS.post(event);

        if (msg instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocity = (SPacketEntityVelocity) msg;
            if (mc.world != null) {
                Entity entity = mc.world.getEntityByID(velocity.getEntityID());
                Entity vehicle = mc.player.getLowestRidingEntity();
                if ((entity == vehicle) && (vehicle != mc.player) && (vehicle.canPassengerSteer())) {
                    cancel = true;
                }
            }
        } else if (msg instanceof SPacketMoveVehicle) {
            SPacketMoveVehicle moveVehicle = (SPacketMoveVehicle) msg;
            Entity vehicle = mc.player.getLowestRidingEntity();
            if ((vehicle == mc.player) || (!vehicle.canPassengerSteer()) || (vehicle.getDistance(moveVehicle.getX(), moveVehicle.getY(), moveVehicle.getZ()) <= 25D)) {
                cancel = true;
            }
        }

        if (cancel) return;

        super.channelRead(ctx, msg);
    }

}
