import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from app.constants import get_assets_dir
from app.content_loader import ContentLoader
from app.validators import validate_level


def main() -> int:
    assets_dir = get_assets_dir()
    loader = ContentLoader(assets_dir)
    index = loader.load_json("data/levels.json")

    errors: list[str] = []
    for entry in index["levels"]:
        level = loader.load_level(entry["data_file"])
        errors.extend(validate_level(level, assets_dir))

    if errors:
        print("Content validation failed:")
        for error in errors:
            print(f"- {error}")
        return 1

    print("Content validation passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())