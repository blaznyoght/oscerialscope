
java.nio.ByteBuffer ibBuffer = java.nio.ByteBuffer.wrap(inBuf)
ibBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
java.nio.ByteBuffer obBuffer = java.nio.ByteBuffer.allocate(ibBuffer.capacity())
obBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
java.util.Map ctx = new java.util.HashMap()

%script%

for(int i=0; i<ibBuffer.capacity(); i+=2) {
	def inShort = ibBuffer.getShort(i)
	def outShort = doLoop(inShort, ctx)
	obBuffer.putShort(outShort)
}
outBuf.write(obBuffer.array())
