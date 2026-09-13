package powercrystals.minefactoryreloaded.net;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import powercrystals.minefactoryreloaded.core.UtilInventory;
import powercrystals.minefactoryreloaded.entity.EntityRocket;
import powercrystals.minefactoryreloaded.net.ServerPacketHandler.MFRMessage;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactory;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityAutoAnvil;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityAutoDisenchanter;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityAutoEnchanter;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityAutoJukebox;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityAutoSpawner;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityBlockSmasher;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityChronotyper;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityChunkLoader;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityDeepStorageUnit;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityEjector;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityEnchantmentRouter;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityHarvester;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityItemRouter;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityMobRouter;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityPlanter;
import powercrystals.minefactoryreloaded.tile.rednet.TileEntityRedNetLogic;

public class ServerPacketHandler implements IMessageHandler<MFRMessage, IMessage> {

    @Override
    public IMessage onMessage(MFRMessage message, MessageContext ctx) {

        if (message.packet != null) ctx.getServerHandler()
            .sendPacket(message.packet);
        return null;
    }

    private static TileEntity readTile(World world, ByteBuf data) {

        int x = data.readInt();
        int y = data.readInt();
        int z = data.readInt();
        return world.getTileEntity(x, y, z);
    }

    private static Packet readData(ByteBuf data) {

        World world = DimensionManager.getWorld(data.readInt());

        switch (data.readUnsignedShort()) {
            case Packets.HAMUpdate:
                var te = readTile(world, data);
                if (te instanceof TileEntityFactory factory && factory.hasHAM()) {
                    return factory.getHAM()
                        .getUpgradePacket();
                }
                break;
            case Packets.EnchanterButton: // client -> server: autoenchanter GUI buttons
                te = readTile(world, data);

                byte amt = data.readByte();
                if (te instanceof TileEntityAutoEnchanter enchanter) {
                    enchanter.setTargetLevel(enchanter.getTargetLevel() + amt);
                } else if (te instanceof TileEntityBlockSmasher smasher) {
                    smasher.setFortune(smasher.getFortune() + amt);
                } else if (te instanceof TileEntityAutoDisenchanter disenchanter) {
                    disenchanter.setRepeatDisenchant(amt == 1);
                }
                break;
            case Packets.HarvesterButton: // client -> server: harvester setting
                te = readTile(world, data);

                if (te instanceof TileEntityHarvester harvester) {
                    harvester.getSettings()
                        .put(ByteBufUtils.readUTF8String(data), data.readBoolean());
                }
                break;
            case Packets.ChronotyperButton: // client -> server: toggle chronotyper
                te = readTile(world, data);

                if (te instanceof TileEntityChronotyper chronotyper) {
                    chronotyper.setMoveOld(!chronotyper.getMoveOld());
                } else if (te instanceof TileEntityDeepStorageUnit dsu) {
                    dsu.setIsActive(!dsu.isActive());
                    dsu.markForUpdate();
                    Packets.sendToAllPlayersWatching(te);
                }
                break;
            case Packets.AutoJukeboxButton: // client -> server: copy record
                te = readTile(world, data);

                if (te instanceof TileEntityAutoJukebox jukebox) {
                    int button = data.readByte();
                    if (button == 1) jukebox.playRecord();
                    else if (button == 2) jukebox.stopRecord();
                    else if (button == 3) jukebox.copyRecord();
                }
                break;
            case Packets.AutoSpawnerButton: // client -> server: toggle autospawner
                te = readTile(world, data);

                if (te instanceof TileEntityAutoSpawner spawner) {
                    spawner.setSpawnExact(!spawner.getSpawnExact());
                }
                break;
            case Packets.CircuitDefinition: // client -> server: request circuit from server
                te = readTile(world, data);

                if (te instanceof TileEntityRedNetLogic logic) {
                    logic.sendCircuitDefinition(data.readInt());
                }
                break;
            case Packets.LogicSetCircuit: // client -> server: set circuit
                te = readTile(world, data);

                int circuit = data.readInt();
                if (te instanceof TileEntityRedNetLogic logic) {
                    logic.initCircuit(circuit, ByteBufUtils.readUTF8String(data));
                    logic.sendCircuitDefinition(circuit);
                }
                break;
            case Packets.LogicSetPin: // client -> server: set pin
                te = readTile(world, data);

                amt = data.readByte();
                int circuitIndex = data.readInt(), pinIndex = data.readInt(), buffer = data.readInt(),
                    pin = data.readInt();
                if (te instanceof TileEntityRedNetLogic logic) {
                    if (amt == 0) {
                        logic.setInputPinMapping(circuitIndex, pinIndex, buffer, pin);
                    } else if (amt == 1) {
                        logic.setOutputPinMapping(circuitIndex, pinIndex, buffer, pin);
                    }
                    logic.sendCircuitDefinition(circuitIndex);
                }
                break;
            case Packets.LogicReinitialize: // client -> server: set circuit
                te = readTile(world, data);
                EntityPlayer player = (EntityPlayer) world.getEntityByID(data.readInt());

                if (te instanceof TileEntityRedNetLogic logic) {
                    logic.reinitialize(player);
                }
                break;
            case Packets.RouterButton: // client -> server: toggle 'levels'/'reject unmapped' mode
                te = readTile(world, data);

                int a = data.readInt();
                if (te instanceof TileEntityEnchantmentRouter enchantRouter) {
                    switch (a) {
                        case 2 -> enchantRouter.setRejectUnmapped(!enchantRouter.getRejectUnmapped());
                        case 1 -> enchantRouter.setMatchLevels(!enchantRouter.getMatchLevels());
                    }
                } else if (te instanceof TileEntityItemRouter itemRouter) {
                    itemRouter.setRejectUnmapped(!itemRouter.getRejectUnmapped());
                } else if (te instanceof TileEntityEjector ejector) {
                    switch (a) {
                        case 1 -> ejector.setIsWhitelist(!ejector.getIsWhitelist());
                        case 2 -> ejector.setIsNBTMatch(!ejector.getIsNBTMatch());
                        case 3 -> ejector.setIsIDMatch(!ejector.getIsIDMatch());
                    }
                } else if (te instanceof TileEntityAutoAnvil anvil) {
                    anvil.setRepairOnly(!anvil.getRepairOnly());
                } else if (te instanceof TileEntityChunkLoader chunkLoader) {
                    chunkLoader.setRadius((short) a);
                } else if (te instanceof TileEntityPlanter planter) {
                    planter.setConsumeAll(!planter.getConsumeAll());
                } else if (te instanceof TileEntityMobRouter mobRouter) {
                    switch (a) {
                        case 1 -> mobRouter.setWhiteList(!mobRouter.getWhiteList());
                        case 2 -> mobRouter.setMatchMode(mobRouter.getMatchMode() + 1);
                        case 3 -> mobRouter.setMatchMode(mobRouter.getMatchMode() - 1);
                    }
                }
                break;
            case Packets.FakeSlotChange: // client -> server: client clicked on a fake slot
                te = readTile(world, data);
                player = (EntityPlayer) world.getEntityByID(data.readInt());

                ItemStack playerStack = player.inventory.getItemStack();
                int slotNumber = data.readInt(), click = data.readByte();
                if (te instanceof IInventory inventory) {
                    if (playerStack == null) {
                        inventory.setInventorySlotContents(slotNumber, null);
                    } else {
                        playerStack = playerStack.copy();
                        playerStack.stackSize = click == 1 ? -1 : 1;
                        ItemStack s = inventory.getStackInSlot(slotNumber);
                        if (!UtilInventory.stacksEqual(s, playerStack)) playerStack.stackSize = 1;
                        else playerStack.stackSize = Math.max(playerStack.stackSize + s.stackSize, 1);
                        inventory.setInventorySlotContents(slotNumber, playerStack);
                    }
                }
                break;
            case Packets.RocketLaunch: // client -> server: client firing SPAMR missile
                Entity owner = world.getEntityByID(data.readInt());
                int t = data.readInt();
                Entity target = null;
                if (t != Integer.MIN_VALUE) {
                    target = world.getEntityByID(t);
                }

                if (owner instanceof EntityLivingBase living) {
                    EntityRocket r = new EntityRocket(world, living, target);
                    world.spawnEntityInWorld(r);
                }
                break;
        }
        return null;
    }

    public static class MFRMessage implements IMessage {

        public ByteBuf buf;
        public Packet packet;

        public MFRMessage() {

        }

        public MFRMessage(short packet, TileEntity te, Object... args) {

            ByteBuf buff = Unpooled.buffer();
            buff.writeInt(te.getWorldObj().provider.dimensionId);
            buff.writeShort(packet);
            buff.writeInt(te.xCoord);
            buff.writeInt(te.yCoord);
            buff.writeInt(te.zCoord);
            handleObjects(buff, args);
            buf = buff;
        }

        public MFRMessage(short packet, Entity e, Object... args) {

            ByteBuf buff = Unpooled.buffer();
            buff.writeInt(e.worldObj.provider.dimensionId);
            buff.writeShort(packet);
            buff.writeInt(e.getEntityId());
            handleObjects(buff, args);
            buf = buff;
        }

        @Override
        public void fromBytes(ByteBuf buf) {

            packet = readData(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {

            buf.writeBytes(this.buf);
        }

        private static void handleObjects(ByteBuf data, Object[] objects) {

            for (Object obj : objects) {
                Class<?> objClass = obj.getClass();
                if (objClass.equals(Integer.class)) {
                    data.writeInt((Integer) obj);
                } else if (objClass.equals(Boolean.class)) {
                    data.writeBoolean((Boolean) obj);
                } else if (objClass.equals(Byte.class)) {
                    data.writeByte((Byte) obj);
                } else if (objClass.equals(Short.class)) {
                    data.writeShort((Short) obj);
                } else if (objClass.equals(String.class)) {
                    ByteBufUtils.writeUTF8String(data, (String) obj);
                } else if (Entity.class.isAssignableFrom(objClass)) {
                    data.writeInt(((Entity) obj).getEntityId());
                } else if (objClass.equals(Double.class)) {
                    data.writeDouble((Double) obj);
                } else if (objClass.equals(Float.class)) {
                    data.writeFloat((Float) obj);
                } else if (objClass.equals(Long.class)) {
                    data.writeLong((Long) obj);
                }
            }
        }
    }
}
