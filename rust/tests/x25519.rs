use encrust::x25519::{encrust_x25519_public_v1, encrust_x25519_shared_v1};

fn decode(hex: &str) -> [u8; 32] {
    std::array::from_fn(|i| u8::from_str_radix(&hex[2 * i..2 * i + 2], 16).unwrap())
}

#[test]
fn rfc7748_fixed_buffers_and_low_order() {
    let alice = decode("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a");
    let bob_public = decode("de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f");
    let mut output = [0; 32];
    unsafe {
        assert_eq!(
            encrust_x25519_public_v1(alice.as_ptr(), output.as_mut_ptr()),
            0
        );
        assert_eq!(
            output,
            decode("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a")
        );
        assert_eq!(
            encrust_x25519_shared_v1(alice.as_ptr(), bob_public.as_ptr(), output.as_mut_ptr()),
            0
        );
        assert_eq!(
            output,
            decode("4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742")
        );
        assert_eq!(
            encrust_x25519_shared_v1(alice.as_ptr(), [0; 32].as_ptr(), output.as_mut_ptr()),
            -2
        );
        assert_eq!(output, [0; 32]);
    }
}
