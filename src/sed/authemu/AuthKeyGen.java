package sed.authemu;
public final class AuthKeyGen
{
    public AuthKeyGen()
    {
    }

    private static int tailbytesToInt(byte input[])
    {
        int i = input[input.length - 1] & 0xf;
        return ((input[i] & 0x7f) << 24) + ((input[i + 1] & 0xff) << 16) + ((input[i + 2] & 0xff) << 8) + (input[i + 3] & 0xff);
    }

    /**
     * Calculates an authentication key.
     * Only called by 1 function: {@link MainGUI#calcAuthKey}
     * 
     * @param token All 20 bytes of the security token.
     * @param time_divided (currentTimeMillis() + time_offset) / 30000
     * @return
     */
    public static int calcAuthKey(byte token[], long time_divided)
    {
        HMAC hmac = new HMAC(HashSHA1.getHashSHAObject());
        
        hmac.update(token);
        
        byte time_bytes[] = new byte[8];
        time_bytes[0] = (byte)(int)(time_divided >>> 56);
        time_bytes[1] = (byte)(int)(time_divided >>> 48);
        time_bytes[2] = (byte)(int)(time_divided >>> 40);
        time_bytes[3] = (byte)(int)(time_divided >>> 32);
        time_bytes[4] = (byte)(int)(time_divided >>> 24);
        time_bytes[5] = (byte)(int)(time_divided >>> 16);
        time_bytes[6] = (byte)(int)(time_divided >>> 8);
        time_bytes[7] = (byte)(int)(time_divided);
        
        hmac.updateHash2(time_bytes);
        byte hmacDigest[] = hmac.getDigest();
        
        return tailbytesToInt(hmacDigest) % ((int)100000000);
    }
}