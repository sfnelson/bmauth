package sed.authemu;

public final class HashSHA1
{
    private byte pad[];
    private long pad_index;
    private int context[];
    private static HashSHAFactoryImpl hash_sha_factory = new HashSHAFactoryImpl(null);

    static class StubEmptySHA
    {
    }

    private static class HashSHAFactoryImpl extends HashSHA1FactoryAbstract
    {

        public final HashSHA1 getInstance()
        {
            return new HashSHA1();
        }

        private HashSHAFactoryImpl()
        {
        }

        HashSHAFactoryImpl(StubEmptySHA n1)
        {
            this();
        }
    }

    public static HashSHA1FactoryAbstract getHashSHAObject()
    {
        return hash_sha_factory;
    }

    public HashSHA1()
    {
        pad = new byte[64];
        context = new int[5];
        initContext();
    }

    /**
     * Copy constructor.
     * @param other original instance
     */
    private HashSHA1(HashSHA1 other)
    {
        pad = new byte[64];
        context = new int[5];
        pad_index = other.pad_index;
        System.arraycopy(other.context, 0, context, 0, 5);
        System.arraycopy(other.pad, 0, pad, 0, (int)(pad_index & 63L));
    }

    public final HashSHA1 getInstanceCopy()
    {
        return new HashSHA1(this);
    }

    public final int hashCode()
    {
        byte d[] = getDigest();
        return bytesToDWord(d, 0) + bytesToDWord(d, 1) + bytesToDWord(d, 2) + bytesToDWord(d, 3) + bytesToDWord(d, 4);
    }

    public final int getBlockSize()
    {
        return 64;
    }

    public final void initContext()
    {
        pad_index = 0L;
        context[0] = 0x67452301;
        context[1] = 0xefcdab89;
        context[2] = 0x98badcfe;
        context[3] = 0x10325476;
        context[4] = 0xc3d2e1f0;
    }

    public final void updateEngine(byte input[], int offset, int len)
    {
        if(offset < 0)
            throw new ArrayIndexOutOfBoundsException("index is negative");
        if(len < 0)
            throw new NegativeArraySizeException("size is negative");
        if(offset + len < offset || offset + len > input.length)
            throw new ArrayIndexOutOfBoundsException("index and/or size are too large");
        int i = 0;
        int k;
        for(; len > 0; len -= k)
        {
            int j = (int)pad_index & 0x3f;
            if((k = len <= 64 - j ? len : 64 - j) == 64)
            {
                hashBlock(input, offset + i, context);
            } else
            {
                System.arraycopy(input, offset + i, pad, j, k);
                if(j + k == 64)
                    hashBlock(pad, 0, context);
            }
            pad_index += k;
            i += k;
        }

    }

    public final void updateEngine(byte input[])
    {
        updateEngine(input, 0, input.length);
    }

    public final byte[] getDigest()
    {
        int j = (int)pad_index & 0x3f;
        pad[j] = -128;
        boolean flag = false;
        int k;
        if((k = 56 - (j + 1)) < 0)
        {
            k += 8;
            flag = true;
        }
        for(int i = 0; i < k; i++)
            pad[j + 1 + i] = 0;

        int ai[] = new int[5];
        System.arraycopy(context, 0, ai, 0, 5);
        byte abyte0[] = pad;
        if(flag)
        {
            hashBlock(pad, 0, ai);
            abyte0 = new byte[64];
        }
        long l1 = pad_index * 8L;
        abyte0[56] = (byte)(int)(l1 >>> 56);
        abyte0[57] = (byte)(int)(l1 >>> 48);
        abyte0[58] = (byte)(int)(l1 >>> 40);
        abyte0[59] = (byte)(int)(l1 >>> 32);
        abyte0[60] = (byte)(int)(l1 >>> 24);
        abyte0[61] = (byte)(int)(l1 >>> 16);
        abyte0[62] = (byte)(int)(l1 >>> 8);
        abyte0[63] = (byte)(int)l1;
        hashBlock(abyte0, 0, ai);
        byte digest[] = new byte[20];
        for(int j1 = 0; j1 < 20;)
        {
            int k1 = ai[j1 / 4];
            digest[j1++] = (byte)(k1 >>> 24);
            digest[j1++] = (byte)(k1 >>> 16);
            digest[j1++] = (byte)(k1 >>> 8);
            digest[j1++] = (byte)k1;
        }

        return digest;
    }

    private static int rotDwordLeft(int shift, int x) {
    	return (x << shift) + (x >>> (32-shift));
    }

    private static int bytesToDWord(byte abyte0[], int offset)
    {
        return ((abyte0[offset] & 0xff) << 24) + ((abyte0[offset + 1] & 0xff) << 16) + ((abyte0[offset + 2] & 0xff) << 8) + (abyte0[offset + 3] & 0xff);
    }

    private static void hashBlock(byte input[], int offset, int context[])
    {
        int ai1[] = new int[80];
        int k;
        for(k = 0; k < 16; k++)
            ai1[k] = bytesToDWord(input, offset + k * 4);

        for(; k < 80; k++)
            ai1[k] = rotDwordLeft(1, ai1[k - 3] ^ ai1[k - 8] ^ ai1[k - 14] ^ ai1[k - 16]);

        int i1 = context[0];
        int j1 = context[1];
        int k1 = context[2];
        int l1 = context[3];
        int i2 = context[4];
        k = 0;
        
        // 4 rounds 
        do
        {
            i2 += rotDwordLeft(5, i1) + (j1 & k1 | ~j1 & l1) + ai1[k++] + 0x5a827999;
            j1 = rotDwordLeft(30, j1);
            l1 += rotDwordLeft(5, i2) + (i1 & j1 | ~i1 & k1) + ai1[k++] + 0x5a827999;
            i1 = rotDwordLeft(30, i1);
            k1 += rotDwordLeft(5, l1) + (i2 & i1 | ~i2 & j1) + ai1[k++] + 0x5a827999;
            i2 = rotDwordLeft(30, i2);
            j1 += rotDwordLeft(5, k1) + (l1 & i2 | ~l1 & i1) + ai1[k++] + 0x5a827999;
            l1 = rotDwordLeft(30, l1);
            i1 += rotDwordLeft(5, j1) + (k1 & l1 | ~k1 & i2) + ai1[k++] + 0x5a827999;
            k1 = rotDwordLeft(30, k1);
        } while(k < 20);
        do
        {
            i2 += rotDwordLeft(5, i1) + (j1 ^ k1 ^ l1) + ai1[k++] + 0x6ed9eba1;
            j1 = rotDwordLeft(30, j1);
            l1 += rotDwordLeft(5, i2) + (i1 ^ j1 ^ k1) + ai1[k++] + 0x6ed9eba1;
            i1 = rotDwordLeft(30, i1);
            k1 += rotDwordLeft(5, l1) + (i2 ^ i1 ^ j1) + ai1[k++] + 0x6ed9eba1;
            i2 = rotDwordLeft(30, i2);
            j1 += rotDwordLeft(5, k1) + (l1 ^ i2 ^ i1) + ai1[k++] + 0x6ed9eba1;
            l1 = rotDwordLeft(30, l1);
            i1 += rotDwordLeft(5, j1) + (k1 ^ l1 ^ i2) + ai1[k++] + 0x6ed9eba1;
            k1 = rotDwordLeft(30, k1);
        } while(k < 40);
        do
        {
            i2 += rotDwordLeft(5, i1) + (j1 & k1 | j1 & l1 | k1 & l1) + ai1[k++] + 0x8f1bbcdc;
            j1 = rotDwordLeft(30, j1);
            l1 += rotDwordLeft(5, i2) + (i1 & j1 | i1 & k1 | j1 & k1) + ai1[k++] + 0x8f1bbcdc;
            i1 = rotDwordLeft(30, i1);
            k1 += rotDwordLeft(5, l1) + (i2 & i1 | i2 & j1 | i1 & j1) + ai1[k++] + 0x8f1bbcdc;
            i2 = rotDwordLeft(30, i2);
            j1 += rotDwordLeft(5, k1) + (l1 & i2 | l1 & i1 | i2 & i1) + ai1[k++] + 0x8f1bbcdc;
            l1 = rotDwordLeft(30, l1);
            i1 += rotDwordLeft(5, j1) + (k1 & l1 | k1 & i2 | l1 & i2) + ai1[k++] + 0x8f1bbcdc;
            k1 = rotDwordLeft(30, k1);
        } while(k < 60);
        do
        {
            i2 += rotDwordLeft(5, i1) + (j1 ^ k1 ^ l1) + ai1[k++] + 0xca62c1d6;
            j1 = rotDwordLeft(30, j1);
            l1 += rotDwordLeft(5, i2) + (i1 ^ j1 ^ k1) + ai1[k++] + 0xca62c1d6;
            i1 = rotDwordLeft(30, i1);
            k1 += rotDwordLeft(5, l1) + (i2 ^ i1 ^ j1) + ai1[k++] + 0xca62c1d6;
            i2 = rotDwordLeft(30, i2);
            j1 += rotDwordLeft(5, k1) + (l1 ^ i2 ^ i1) + ai1[k++] + 0xca62c1d6;
            l1 = rotDwordLeft(30, l1);
            i1 += rotDwordLeft(5, j1) + (k1 ^ l1 ^ i2) + ai1[k++] + 0xca62c1d6;
            k1 = rotDwordLeft(30, k1);
        } while(k < 80);
        context[0] += i1;
        context[1] += j1;
        context[2] += k1;
        context[3] += l1;
        context[4] += i2;
    }

}