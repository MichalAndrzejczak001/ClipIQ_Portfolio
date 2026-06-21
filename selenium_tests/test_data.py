import random
import string

from faker import Faker

fake = Faker()


def youtube_url() -> str:
    chars = string.ascii_letters + string.digits + "-_"
    video_id = fake.lexify(text="?" * 11, letters=chars)
    return f"https://www.youtube.com/watch?v={video_id}"


def tiktok_url() -> str:
    # username must not contain "." — AnalysisController rejects URLs containing ".."
    chars = string.ascii_lowercase + string.digits + "_"
    username = fake.lexify(text="?" * random.randint(6, 12), letters=chars)
    video_id = fake.numerify("#" * 19)
    return f"https://www.tiktok.com/@{username}/video/{video_id}"
