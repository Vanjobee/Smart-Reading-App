from pathlib import Path
import os

APP_NAME = "Smart Guide Basic Reading"
APP_PREF_PREFIX = "smart_reading_app.v1."
ASSETS_DIR_NAME = "assets"


def get_assets_dir() -> Path:
    """Return bundled asset path in both local dev and packaged app builds."""
    default_assets_dir = Path(__file__).resolve().parent.parent / ASSETS_DIR_NAME
    return Path(os.environ.get("FLET_ASSETS_DIR", str(default_assets_dir))).resolve()