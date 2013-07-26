/*
 * EID.h
 *
 * Copyright (C) 2011 IBR, TU Braunschweig
 *
 * Written-by: Johannes Morgenroth <morgenroth@ibr.cs.tu-bs.de>
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
 *
 */

#ifndef EID_H_
#define EID_H_

#include <string>
#include "ibrcommon/Exceptions.h"
#include <ibrdtn/data/Number.h>

using namespace std;

namespace dtn
{
	namespace data
	{
		class EID
		{
		public:
			static const std::string DEFAULT_SCHEME;
			static const std::string CBHE_SCHEME;

			EID();
			EID(const std::string &scheme, const std::string &ssp);
			EID(const std::string &value);

			/**
			 * Constructor for CBHE EIDs.
			 * @param node Node number.
			 * @param application Application number.
			 */
			EID(const dtn::data::Number &node, const dtn::data::Number &application);

			virtual ~EID();

			bool operator==(const EID &other) const;

			bool operator==(const std::string &other) const;

			bool operator!=(const EID &other) const;

			EID add(const std::string &other) const {
				return EID(this->getString() + other);
			}

			bool sameHost(const std::string &other) const;
			bool sameHost(const EID &other) const;

			bool operator<(const EID &other) const;
			bool operator>(const EID &other) const;

			std::string getString() const;

			void setApplication(const dtn::data::Number &app) throw ();
			void setApplication(const std::string &app) throw ();
			std::string getApplication() const throw ();
			std::string getHost() const throw ();
			const std::string getScheme() const;
			const std::string getSSP() const;

			std::string getDelimiter() const;

			/**
			 * Return the EID with stripped application part
			 * @return The EID without any application specific part
			 */
			EID getNode() const throw ();

			bool hasApplication() const;

			/**
			 * check if a EID is compressable.
			 * @return True, if the EID is compressable.
			 */
			bool isCompressable() const;

			/**
			 * Determine if this EID is null
			 * @return True, if the EID is null
			 */
			bool isNone() const;

			/**
			 * Get the compressed EID as two numeric values. Both values
			 * are set to zero if the EID is not compressable.
			 * @return A pair of two numeric values.
			 */
			typedef std::pair<Number, Number> Compressed;
			Compressed getCompressed() const;

		private:
			enum Scheme {
				SCHEME_DTN = 0,
				SCHEME_CBHE = 1,
				SCHEME_EXTENDED = 2
			} _scheme_type;

			EID(const Scheme scheme_type, const std::string &scheme, const std::string &ssp, const std::string &application);

			static Scheme resolveScheme(const std::string &s);

			/**
			 * Extract the CBHE node and application from an SSP string
			 */
			static void extractCBHE(const std::string &ssp, Number &node, Number &app);

			/**
			 * Extract the DTN node and application from an SSP string
			 */
			static void extractDTN(const std::string &ssp, std::string &node, std::string &application);

			// abstract values
			std::string _scheme;
			std::string _ssp;

			// DTN scheme
			// the ssp carries the node part
			std::string _application;

			// CBHE scheme
			Number _cbhe_node;
			Number _cbhe_application;
		};
	}
}

#endif /* EID_H_ */
