FROM jupyter/scipy-notebook:6c3390a9292e

WORKDIR /home/jovyan/

# Modules
COPY requirements.txt /home/jovyan/requirements.txt
RUN pip install -r /home/jovyan/requirements.txt

# Add files
COPY notebooks /home/jovyan/notebooks
COPY postBuild /home/jovyan/postBuild

# Allow user to write to directory
USER root
RUN chown -R $NB_USER /home/jovyan \
    && chmod -R 774 /home/jovyan \
    && rm -fR /home/jovyan/work \
    && /home/jovyan/postBuild
USER $NB_USER

# Expose the notebook port
EXPOSE 8888
