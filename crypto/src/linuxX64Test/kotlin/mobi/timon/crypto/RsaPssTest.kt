package mobi.timon.crypto

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class RsaPssTest {
    @Test
    fun independentOpenSslSignatures() {
        val n = "b796190f7f66e2da2e327be596218f8c7754e269066572afb5cac297cf82d6afa686f8d512b4ba452befda5e12d13938d6170397ebba4b42cfa3287edd84097643a70cdc7666929d14ffbc50296a6ed6ef3bf23474d6a7e72801d7751a2fdfec40735d3d53e448acd7280e78dd8f82a08c7c7a958a4ca3b04b732ca7677cdb7be8283b21540cdaa0421a983f160b619ba8b58f9bced766d9fe132f5ee9dadc98638b7ff2a2adad09f7f2f8efe103258e302ebe482e3458f97d6f0e5eefea73cbadfff0fa0a9ee630a83761f2a98f1e000b5d8f16d8e15b4ee838b3b10d2a4159496d0b65095293f24e490b371570ed8ce9674a58f30d2528924a2137fba7da2f".hexToByteArray()
        val e = byteArrayOf(1, 0, 1)
        fun checkVector(digestHex: String, signatureHex: String, salt: Int) {
            val digest = digestHex.hexToByteArray()
            val sig = signatureHex.hexToByteArray()
            assertTrue(Rsa.verifyPss(n, e, digest, sig, salt))
            assertFalse(Rsa.verifyPss(n, e, digest, sig, salt + 1))
            assertFalse(Rsa.verifyPss(n, e, digest, n, salt))
            assertFalse(Rsa.verifyPss(n, e, digest, byteArrayOf(), salt))
            sig[0] = (sig[0].toInt() xor 1).toByte()
            assertFalse(Rsa.verifyPss(n, e, digest, sig, salt))
            assertFailsWith<IllegalArgumentException> { Rsa.verifyPss(n, byteArrayOf(2), digest, sig, salt) }
        }
        checkVector("2367ee639b5d89a7c5b040b036018634dcbf344fa01efa91c82d2277750a87ad",
            "3322cae922ee5d0ac8b674b82010000e644c61d092ad9cc4c4c16091c4887cd7598a298f0f462fe9dba306c787c39346afc5742d67027ad37a3e9a45e0679e167651779c7c482667c60710d8ccced6ec208836d67fb9ccca486c6e01d03a860fb19f5c24917ef5f8d928cf15a361747b56e1a721897d51ace72f75b20339eed72a44cd9e9e7dc2377c04eb23aa2532f81d31b57c17e441e6fc02b1dc77b28927ccb49751484d98831dca2f8b9064d9a84e9296302dc323f6f036a4fe93463086235769aa860b58210cec03d0f20c8bfffdb39251753a712199fa94a7af9fd7a2809fe155d481daf661380f919c99c1ee545da8243f0062f93542bb7edd9e54d1", 32)
        checkVector("fd5c1ed88eec0bf0dd804974dc29aac599ed8cac2af11e7b35586e439e2747af8a2420e1bae1fb702ce3efe069215916",
            "31922a37303cbbfec0586b2fe7cf5f252afc698150cb767eb39d0665cc3f0734e3c510b666f710325b3234dc0851367ab4c38f99ae34f2209614c094f5e2c39a8b9756dd08cec01b78305c22897526cb6a4f78934c1776f1fe1d29c6339e2cb589a881cdc64fb471ae814ef8617de253dcd02afeb643d9d353951ae2ec8da231703c66aff6f73836dd9dc482def9aa5b4f57c9d55ffc71b93a88209e960614c744e7f125b74f4c623c7a5e53cf548715f0ff1eccd46741449dc657f28e669807003afb3772e523fa69511203394f1ad2dbd7cfa4e1e498c5ea4a73b50f16a48e1f701ac5c91bbb5b9b3f7ddacac4b4fcf76d94868fea93ea33637bd34864e15e", 0)
        checkVector("c911a5ffd4867f26a4f88ed520c18c543e7af7252cff0a8082631f87ac82759c7a33146ab5676216202f84e9b0936b88cb3b926fa173bf26711399d6a17d7391",
            "628014f352f28eac16416aa1857d9e2d5985e66bb22ab92152debae47cc0b0c1ec8323889c8c27127d5519a6035d2f6255a6be6e9148298b56b9c5508a20ea45ab3cd622f5799b6175b15089ea1708a8e9f7743b4ac59c69f485051e4232ff334c4956ebb406df69f587630cf2ab01c05b7bd9aa80d8b9f91c13aa7f275c1e531586d63126808e9968f512c16829d99e361898feb094e40fe396e043488990c7481af891f45f28ca0d39008a8e9a85cdcf945520076aee5daace010b1a9172799e193e7c45c5563fa952415dd3fce6e619354ab0382bf49cda95259760ae87cabd28a33411c6396edb6b8130ff100453fe11a325b52150adfa8d4d21a726cc67", 19)
    }
}
