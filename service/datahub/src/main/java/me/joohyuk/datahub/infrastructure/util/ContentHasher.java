package me.joohyuk.datahub.infrastructure.util;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import me.joohyuk.datahub.domain.vo.ContentHash;

/**
 * SHA-256 콘텐츠 해시 계산을 책임지는 유틸리티입니다.
 *
 * <p>{@link DigestInputStream} 패턴을 활용하여 스트림을 단일 패스로 읽는 동안 해시를 부수적으로
 * 계산합니다. 파일을 두 번 읽지 않는 성능 최적화입니다.
 *
 * <h3>사용 패턴 (2단계)</h3>
 * <pre>{@code
 *   // Phase 1: 스트림을 감싸서 저장소에 전달
 *   var hashingStream = ContentHasher.wrap(fileInputStream);
 *   fileStorage.store(hashingStream, metadata, scope);
 *
 *   // Phase 2: 스트림이 완전히 소비된 후 해시 추출
 *   ContentHash contentHash = hashingStream.getContentHash();
 * }</pre>
 */
public final class ContentHasher {

  private static final String ALGORITHM = "SHA-256";

  private ContentHasher() {}

  /**
   * InputStream을 감싸서 소비되는 동안 SHA-256 해시를 계산하는 스트림을 반환합니다.
   *
   * @param input 원본 입력 스트림
   * @return SHA-256 해시를 동시에 계산하는 {@link HashingInputStream}
   */
  public static HashingInputStream wrap(InputStream input) {
    return new HashingInputStream(input, newSha256Digest());
  }

  /**
   * 2단계 해시 계산 패턴을 캡슐화하는 스트림 타입입니다.
   *
   * <p>내부적으로 {@link DigestInputStream}을 위임하여 스트림 읽기 중 해시를 자동으로 업데이트하고,
   * {@link #getContentHash()}를 통해 완성된 해시를 도메인 값 타입({@link ContentHash})으로 반환합니다.
   *
   * <p><b>주의:</b> {@link #getContentHash()}는 스트림이 <em>완전히</em> 소비된 후에만
   * 올바른 값을 반환합니다.
   */
  public static final class HashingInputStream extends FilterInputStream {

    private final MessageDigest digest;

    HashingInputStream(InputStream in, MessageDigest digest) {
      super(new DigestInputStream(in, digest));
      this.digest = digest;
    }

    /**
     * 스트림이 완전히 소비된 후 계산된 콘텐츠 해시를 반환합니다.
     *
     * @return SHA-256 콘텐츠 해시
     */
    public ContentHash getContentHash() {
      return ContentHash.of(HexFormat.of().formatHex(digest.digest()));
    }
  }

  /**
   * SHA-256 MessageDigest 인스턴스를 생성합니다. SHA-256은 JCE 필수 알고리즘이므로 항상
   * 사용 가능합니다.
   */
  private static MessageDigest newSha256Digest() {
    try {
      return MessageDigest.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(ALGORITHM + " is a required JCE algorithm", e);
    }
  }
}
