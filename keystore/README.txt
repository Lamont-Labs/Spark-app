Store your Play App Signing upload key here.

Generate:
keytool -genkeypair -v -keystore spark-release.keystore -alias spark -keyalg RSA -keysize 2048 -validity 10000

Do NOT commit keystore to git.
