package bnetp;

import java.io.IOException;

public class BNetPacketReader extends _PacketReader<BNetProtocolPacketId> {

    public BNetPacketReader(BNetInputStream rawis) throws IOException {
        super(rawis);
    }

    @Override
    protected void parse(BNetInputStream is) throws IOException {
        packetId = BNetProtocolPacketId.values()[is.readByte() & 0x000000FF];
        packetLength = is.readWord() & 0x0000FFFF;
        assert(packetLength >= 4);

        data = new byte[packetLength-4];
        for(int i = 0; i < packetLength-4; i++) {
            data[i] = is.readByte();
        }
    }
}
