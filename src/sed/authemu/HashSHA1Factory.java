package sed.authemu;
final class HashSHA1Factory extends HashSHA1FactoryAbstract
{

    private HashSHA1Factory()
    {
    }

    public final HashSHA1 getInstance()
    {
        return new HashSHA1();
    }
}