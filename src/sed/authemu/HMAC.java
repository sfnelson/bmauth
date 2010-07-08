package sed.authemu;
public final class HMAC
{

    public HMAC(HashSHA1FactoryAbstract h1)
    {
        hash1 = h1.getInstance();
        hash2 = h1.getInstance();
    }

    public final int hashCode()
    {
        return hash1.hashCode() + hash2.hashCode();
    }

    public final void update(byte input[])
    {
        update(input, 0, input.length);
    }

    private void update(byte input[], int offset, int len)
    {
        if(offset < 0)
            throw new ArrayIndexOutOfBoundsException("index is negative");
        if(len < 0)
            throw new NegativeArraySizeException("size is negative");
        if(offset + len < offset || offset + len > input.length)
            throw new ArrayIndexOutOfBoundsException("index and/or size are too large");
        int k = hash1.getBlockSize();
        if(len > k)
        {
            hash1.initContext();
            hash1.updateEngine(input, offset, len);
            len = (input = hash1.getDigest()).length;
        }
        byte abyte0[] = new byte[k];
        int i;
        for(i = 0; i < len; i++)
            abyte0[i] = (byte)(input[i] ^ 0x36);

        for(; i < k; i++)
            abyte0[i] = 54;

        hash2.initContext();
        hash2.updateEngine(abyte0);
        for(i = 0; i < len; i++)
            abyte0[i] = (byte)(input[i] ^ 0x5c);

        for(; i < k; i++)
            abyte0[i] = 92;

        hash1.initContext();
        hash1.updateEngine(abyte0);
    }

    public final void updateHash2(byte input[])
    {
        hash2.updateEngine(input);
    }

    public final byte[] getDigest()
    {
        HashSHA1 result;
        (result = hash1.getInstanceCopy()).updateEngine(hash2.getDigest());
        return result.getDigest();
    }

    protected final HashSHA1 hash1;
    protected final HashSHA1 hash2;
}