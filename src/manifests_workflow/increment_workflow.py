import ruamel.yaml
import logging
from typing import Set

class IncrementWorkflow:

    def __init__(self, filename: str) -> None:
        self.filename = filename
        self.yaml = ruamel.yaml.YAML()
        self.yaml.explicit_start = True  # type: ignore
        self.yaml.preserve_quotes = True  # type: ignore
        self.yaml.indent(mapping=2, sequence=4, offset=2)

    def load(self) -> ruamel.yaml.comments.CommentedMap:
        with open(self.filename) as f:
             data = self.yaml.load(f)
        logging.info(f"Loaded the yaml file {self.filename}")
        return data
    
    def add_branche(self, branch: str) -> Set[str]:
        data = self.load()
        branch_list = list(data["jobs"]["plugin-version-increment-sync"]["strategy"]["matrix"]["branch"])
        branch_list.append(branch)
        self.save(data)
        logging.info(f"Added new version {branch} to the version increment workflow")
        return branch_list

    def save(self, data) -> None:
        with open(self.filename, 'w') as f:
            self.yaml.dump(data, f)
        logging.info(f"Saved the yaml file {self.filename}")

    #@property
    #def branches() -> Set[str]:
    #    pass




