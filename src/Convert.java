public final class Convert {
    /** Revision identifier */
    public static final String MODULE = "<Convert beta 1, CVS $Revision: 1.8 $>";
    /** to mimic sizeof(short) = short length in bytes = 12 (=16bit) */
    public static final int SIZEOF_SHORT = 2;
    /** to mimic sizeof(int) = int length in bytes = 4 (=32bit) */
    public static final int SIZEOF_INT = 4;
    /** to mimic sizeof(long) = long length in bytes = 8 (=64bit) */
    public static final int SIZEOF_LONG = 8;
    /** bit length of byte */
    public static final int BITS_BYTE = 8;
    /** bit length of short */
    public static final int BITS_SHORT = BITS_BYTE * SIZEOF_SHORT;
    /** bit length of int */
    public static final int BITS_INT = BITS_BYTE * SIZEOF_INT;
    /** bit length of long */
    public static final int BITS_LONG = BITS_BYTE * SIZEOF_LONG;
    /** mask a byte in an int */
    public static final int MASK_BYTE = 0xFF;

    /** Avoids instantiation */
    private Convert () {}

    /**
     * Converts the SIZEOF_LONG bytes starting at off within b to a long value.
     * <br />NOTE: the conversion treats the leftmost byte as the lowest byte of
     * the resulting long value
     * @param b a byte[] containing bytes to be converted
     * @param off the offset where to find the bytes to be converted
     * @return a long value
     * @throws IndexOutOfBoundsException if an index violation occurs
     */
    public static long toLong ( byte[] b, int off ) {
        long r = 0;
        for ( int i = SIZEOF_LONG - 1; i >= 0; i-- )
            r |= (((long) b[ off + i ]) & MASK_BYTE) << (i*BITS_BYTE);
        return r;
    }

    /**
     * Converts the SIZEOF_INT bytes starting at off within b to an int value.
     * <br />NOTE: the conversion treats the leftmost byte as the lowest byte of
     * the resulting int value
     * @param b a byte[] containing bytes to be converted
     * @param off the offset where to find the bytes to be converted
     * @return an int value
     * @throws IndexOutOfBoundsException if an index violation occurs
     */
    public static int toInt ( byte[] b, int off ) {
        int r = 0;
        for ( int i = SIZEOF_INT - 1; i >= 0; i-- )
            r |= ((int) (b[ off + i ] & MASK_BYTE)) << (i*BITS_BYTE);
        return r;
    }

    /**
     * Converts a long value to SIZEOF_LONG bytes stored in b starting at off.
     * <br />NOTE: the conversion stores the lowest byte of the long value as
     * the leftmost byte within the sequence
     * @param val a long value to be split up into bytes
     * @param b a byte[] to be written to
     * @param off the offset where to start writing within b
     * @throws IndexOutOfBoundsException if an index violation occurs
     */
    public static void toBytes ( long val, byte[] b, int off ) {
        for ( int i = 0; i < SIZEOF_LONG; i++, val >>= BITS_BYTE )
            b[ off + i ] = (byte) (val & MASK_BYTE);
    }

    /**
     * Converts an int value to SIZEOF_INT bytes stored in b starting at off.
     * <br />NOTE: the conversion stores the lowest byte of the int value as
     * the leftmost byte within the sequence
     * @param val an int value to be split up into bytes
     * @param b a byte[] to be written to
     * @param off the offset where to start writing within b
     * @throws IndexOutOfBoundsException if an index violation occurs
     */
    public static void toBytes ( int val, byte[] b, int off ) {
        for ( int i = 0; i < SIZEOF_INT; i++, val >>= BITS_BYTE )
            b[ off + i ] = (byte) (val & MASK_BYTE);
    }
    /**
     * Converts a byte value to a hexadecimal String.
     * @param b a byte value
     * @return a hexadecimal (upper-case-based) String representation of the
     * byte value
     */
    public static String toHexString ( byte b ) {
        int len = 2;
        byte[] dig = new byte[ len ];
        dig[ 0 ] = (byte) ((b & 0xF0) >> 4);
        dig[ 1 ] = (byte) (b & 0x0F);
        for ( int i = 0; i < len; i++ )
            dig[ i ] += 10 > dig[ i ] ? 48 : 55;
        return new String( dig );
    }

    public static String toHexString ( byte[] b ) {
        if ( null == b )
            return null;
        int len = b.length;
        byte[] hex = new byte[ len << 1 ];
        for ( int i = 0, j = 0; i < len; i++, j+=2 ) {
            hex[ j ] = (byte) ((b[ i ] & 0xF0) >> 4);
            hex[ j ] += 10 > hex[ j ] ? 48 : 55;
            hex[ j + 1 ] = (byte) (b[ i ] & 0x0F);
            hex[ j + 1 ] += 10 > hex[ j + 1 ] ? 48 : 55;
        }
        return new String( hex );
    }

    /**
     * Parses a number from a string.
     * Finds the first recognizable base-10 number (integer or floating point)
     * in the string and returns it as a Number.
     * @param string String to parse
     * @return first recognizable number
     * @exception NumberFormatException if no recognizable number is found
     */
    public static Number toNumber ( String s )
            throws NumberFormatException
    {
        // parsing states
        int INT = 0;
        int FRAC = 1;
        int EXP = 2;
        int p = 0;
        for ( int i = 0; i < s.length(); ++i ) {
            char c = s.charAt( i );
            if ( Character.isDigit( c ) ) {
                int start = i;
                int end = ++i;
                int state = INT;
                if ( start > 0 && s.charAt( start - 1 ) == '.' ) {
                    --start;
                    state = FRAC;
                }
                if ( start > 0 && s.charAt( start - 1 ) == '-' )
                    --start;
                boolean atEnd = false;
                while ( !atEnd && i < s.length() ) {
                    switch ( s.charAt( i ) ) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            end = ++i;
                            break;
                        case '.':
                            if ( state == INT ) {
                                state = FRAC;
                                ++i;
                            } else {
                                atEnd = true;
                            }
                            break;
                        case 'e':
                        case 'E':
                            state = EXP;
                            ++i;
                            if ( i < s.length() && ((c = s.charAt( i )) == '+' || c == '-') )
                                ++i;
                            break;
                        default:
                            atEnd = true;
                    }
                }
                String num = s.substring( start, end );
                try {
                    if ( state == INT )
                        return new Integer( num );
                    else
                        return new Double( num );
                }
                catch ( NumberFormatException e ) {
                    throw new RuntimeException( "internal error: " + e );
                }
            }
        }
        throw new NumberFormatException( s );
    }

    public static String[] toStrings( Object[] o ) {
        if ( null == o )
            return null;
        int len = o.length;
        String[] s = new String[ len ];
        for ( int i = 0; i < len; i++ )
            if ( null != o[ i ] )
                s[ i ] = o[ i ].toString();
        return s;
    }

    /** converts to int with radix 10 and default 0 */
    public static int toInt ( Object o ) {
        return toInt( o, 10, 0 );
    }

    /**
     * Convets an Object to an int value using radix 10.
     * <br />This is a wrapper for
     * <code>{@link #toInt( Object, int ) toInt( o, 10 )}</code>.
     * @param o Object to be converted to integer
     * @return the int value represented by o or 0 if conversion fails.
     */
    public static int toInt ( Object o, int dflt ) {
        return toInt( o, 10, dflt );
    }

    /**
     * Converts any String representation to an int value using a given radix.
     * <br />For <code>null</code> it returns <code>0</code>.<br />
     * For Objects instanceof Number it returns the Object's
     * <code>intValue()</code>.
     * For all other Objects it uses the <code>toString()</code> method to get an
     * appropriate String representation and parses this String using
     * <code>Integer.parseInt</code>.
     * If conversion fails it returns <code>0</code>.
     * @param o Object to be converted to integer
     * @param radix the radix used for conversion
     * @return the int value represented by <code>o</code> or <code>0</code>
     * if conversion fails.
     */
    public static int toInt ( Object o, int radix, int dflt ) {
        if ( null == o ) // shortcut without exception
            return dflt;
        if ( o instanceof Number )
            return ((Number) o).intValue();
        try {
            return Integer.parseInt( o.toString().trim(), radix );
        }
        catch ( Exception e ) {
            return dflt;
        }
    }

    public static long toLong ( Object o ) {
        return toLong( o, 10, 0 );
    }

    public static long toLong ( Object o, int dflt ) {
        return toLong( o, 10, dflt );
    }

    public static long toLong ( Object o, int radix, int dflt ) {
        if ( null == o ) // shortcut without exception
            return dflt;
        if ( o instanceof Number )
            return ((Number) o).longValue();
        try {
            return Long.parseLong( o.toString().trim(), radix );
        }
        catch ( Exception e ) {
            return dflt;
        }
    }

} // class Convert