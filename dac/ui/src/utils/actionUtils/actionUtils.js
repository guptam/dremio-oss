/*
 * Copyright (C) 2017 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { AUTO_PREVIEW_DELAY } from 'constants/Constants';

class ActionUtils {
  shouldLoad(resource) {
    if (!resource) {
      return false;
    }
    const { isInProgress, isInvalid } = resource.get ? resource.toObject() : resource;
    return !isInProgress && isInvalid;
  }

  runAutoPreview(submitForm) {
    clearTimeout(this.autoPreviewTimer);
    this.autoPreviewTimer = setTimeout(submitForm, AUTO_PREVIEW_DELAY);
  }
}

const actionUtils = new ActionUtils();

export default actionUtils;
