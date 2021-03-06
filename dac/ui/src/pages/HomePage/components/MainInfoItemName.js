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
import { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import CopyButton from 'components/Buttons/CopyButton';
import Radium from 'radium';
import Immutable from 'immutable';
import FontIcon from 'components/Icon/FontIcon';
import DatasetItemLabel from 'components/Dataset/DatasetItemLabel';
import { constructFullPath, splitFullPath, getFullPathListFromEntity } from 'utils/pathUtils';
import { getIconDataTypeFromEntity } from 'utils/iconUtils';

@Radium
export default class MainInfoItemName extends Component {

  static propTypes = {
    item: PropTypes.instanceOf(Immutable.Map).isRequired
  };

  static contextTypes = {
    location: PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);
  }

  getHref(entity) {
    const fileType = entity.get('fileType');
    if (entity.get('fileType') === 'file') {
      if (entity.get('queryable')) {
        return entity.getIn(['links', 'query']);
      }
      return {
        ...this.context.location, state: {
          modal: 'DatasetSettingsModal',
          tab: 'format',
          entityType: entity.get('entityType'),
          entityId: entity.get('id'),
          fullPath: entity.get('filePath'),
          query: {then: 'query'}
        }
      };
    }
    if (fileType === 'folder') {
      if (entity.get('queryable')) {
        return entity.getIn(['links', 'query']);
      }
      return entity.getIn(['links', 'self']);
    }
    return {
      ...this.context.location,
      state: {
        ...this.context.location.state,
        originalDatasetVersion: entity.get('datasetConfig') && entity.getIn(['datasetConfig', 'version'])
      },
      pathname: entity.getIn(['links', 'query'])
    };
  }


  renderDatasetItemLabel() {
    const { item } = this.props;
    const type = item.get('entityType');
    const typeIcon = getIconDataTypeFromEntity(item);
    if (type === 'dataset' || type === 'physicalDataset' || type === 'file' && item.get('queryable')
        || type === 'folder' && item.get('queryable')) {
      return (
        <DatasetItemLabel
          name={item.get('name')}
          item={item}
          fullPath={item.get('fullPathList') || item.getIn(['fileFormat', 'fullPath'])
                    || splitFullPath(item.get('filePath'))}
          typeIcon={typeIcon}/>
      );
    }
    return (
      <div style={styles.flexAlign}>
        <FontIcon type={typeIcon}/>
        <div className='last-File' style={styles.fullPath}>{item.get('name')}</div>
      </div>
    );
  }

  render() {
    const { item } = this.props;
    const fileType = item.get('fileType');
    const fullPath = constructFullPath(getFullPathListFromEntity(item));
    const href = this.getHref(item);
    const linkStyle = fileType === 'folder' ? styles.flexAlign : {...styles.flexAlign, ...styles.leafLink};
    const holderClass = fileType + '-path';

    return (
      <div style={[styles.flexAlign, styles.base]} className={holderClass}>
        <Link style={linkStyle} to={href}>
          {this.renderDatasetItemLabel()}
        </Link>
        { fullPath &&
        <CopyButton text={fullPath} title={la('Copy Path')} />
        }
      </div>
    );
  }
}

const styles = {
  base: {
    width: 'calc(100% - 20px)'
  },
  fullPath: {
    width: '100%',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    marginLeft: 5
  },
  flexAlign: {
    display: 'flex',
    alignItems: 'center',
    maxWidth: '100%'
  },
  leafLink: {
    textDecoration: 'none',
    color: '#333'
  }
};
